/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import models.errors.{NinoFormatError, NotFoundError, UnAuthorisedError, InternalError}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, UNAUTHORIZED}
import play.api.mvc.Results.{BadRequest, NotFound, Status}
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.http.JsonErrorHandler
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject._

@Singleton
class ErrorHandler @Inject() (
    config: Configuration,
    auditConnector: AuditConnector,
    httpAuditEvent: HttpAuditEvent
)(implicit ec: ExecutionContext)
    extends JsonErrorHandler(auditConnector, httpAuditEvent, config) {

  import httpAuditEvent.dataEvent
  private val logger: Logger = Logger(this.getClass)

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    logger.warn(
      s"[ErrorHandler][onClientError] error in version 1, for (${request.method}) [${request.uri}] with status:" +
        s" $statusCode and message: $message")
    statusCode match {
      case BAD_REQUEST =>
        auditConnector.sendEvent(dataEvent("ServerValidationError", "Request bad format exception", request))
        Future.successful(BadRequest(NinoFormatError.asJson))
      case NOT_FOUND =>
        auditConnector.sendEvent(dataEvent("ResourceNotFound", "Resource Endpoint Not Found", request))
        Future.successful(NotFound(NotFoundError.asJson))
      case _ =>
        val errorCode = statusCode match {
          case UNAUTHORIZED => UnAuthorisedError
          case _            => InternalError
        }

        auditConnector.sendEvent(
          dataEvent(
            eventType = "ClientError",
            transactionName = s"A client error occurred, status: $statusCode",
            request = request,
            detail = Map.empty
          )
        )

        Future.successful(Status(statusCode)(errorCode.asJson))
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    logger.warn(s"[ErrorHandler][onServerError] Internal server error in version 1, for (${request.method}) [${request.uri}] -> ", ex)

    val (status, errorCode, eventType) = ex match {
      case _: NotFoundException      => (NOT_FOUND, NotFoundError, "ResourceNotFound")
      case _: AuthorisationException => (UNAUTHORIZED, UnAuthorisedError, "ClientError")
      case e: HttpException          => (e.responseCode, NinoFormatError, "ServerValidationError")
      case e: UpstreamErrorResponse if UpstreamErrorResponse.Upstream4xxResponse.unapply(e).isDefined =>
        (e.reportAs, NinoFormatError, "ServerValidationError")
      case e: UpstreamErrorResponse if UpstreamErrorResponse.Upstream5xxResponse.unapply(e).isDefined =>
        (e.reportAs, InternalError, "ServerInternalError")
      case _ => (INTERNAL_SERVER_ERROR, InternalError, "ServerInternalError")
    }

    auditConnector.sendEvent(
      dataEvent(
        eventType = eventType,
        transactionName = "Unexpected error",
        request = request,
        detail = Map("transactionFailureReason" -> ex.getMessage)
      )
    )

    Future.successful(Status(status)(errorCode.asJson))
  }

}