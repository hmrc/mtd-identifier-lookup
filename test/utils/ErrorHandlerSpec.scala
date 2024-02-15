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

import models.errors.{InternalError, NinoFormatError, NotFoundError, UnAuthorisedError}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.mvc.{AnyContentAsEmpty, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, BAD_REQUEST, UNAUTHORIZED, contentAsJson, status}
import support.UnitSpec
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.{DataEvent, TruncationLog}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

class ErrorHandlerSpec extends UnitSpec with GuiceOneAppPerSuite {

  def versionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"

  class Test() {
    val method = "some-method"

    val requestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)

    val auditConnector: AuditConnector = mock[AuditConnector]
    val httpAuditEvent: HttpAuditEvent = mock[HttpAuditEvent]

    val eventTags: Map[String, String] = Map("transactionName" -> "event.transactionName")

    val dataEvent: DataEvent = DataEvent(
      auditSource = "auditSource",
      auditType = "event.auditType",
      eventId = "",
      tags = eventTags,
      detail = Map("test" -> "test"),
      generatedAt = Instant.now()
    )

    (httpAuditEvent
      .dataEvent(_: String, _: String, _: RequestHeader, _: Map[String, String], _: TruncationLog)(_: HeaderCarrier))
      .expects(*, *, *, *, *, *)
      .returns(dataEvent)

    (auditConnector
      .sendEvent(_: DataEvent)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(Future.successful(Success))

    val configuration: Configuration = Configuration(
      "bootstrap.errorHandler.warnOnly.statusCodes"     -> List.empty,
      "bootstrap.errorHandler.suppress4xxErrorMessages" -> false,
      "bootstrap.errorHandler.suppress5xxErrorMessages" -> false
    )

    val handler = new ErrorHandler(configuration, auditConnector, httpAuditEvent)
  }

  "onClientError" should {
    "return 404 with error body" when {
      s"URI not found" in new Test() {

        private val result = handler.onClientError(requestHeader, Status.NOT_FOUND, "test")
        status(result) shouldBe Status.NOT_FOUND

        contentAsJson(result) shouldBe NotFoundError.asJson
      }
    }

    "return 400 with error body" when {
      "JsValidationException thrown and header is supplied" in new Test() {
        private val result = handler.onClientError(requestHeader, BAD_REQUEST, "test")
        status(result) shouldBe BAD_REQUEST

        contentAsJson(result) shouldBe NinoFormatError.asJson
      }
    }
    "return 500 with error body" when {
      "JsValidationException thrown and header is supplied" in new Test() {
        private val result = handler.onClientError(requestHeader, INTERNAL_SERVER_ERROR, "test")
        status(result) shouldBe INTERNAL_SERVER_ERROR

        contentAsJson(result) shouldBe InternalError.asJson
      }
    }

    "return 401 with error body" when {
      "unauthorised and header is supplied" in new Test() {
        private val result = handler.onClientError(requestHeader, UNAUTHORIZED, "test")
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe UnAuthorisedError.asJson
      }
    }

  }

  "onServerError" should {

    "return 404 with error body" when {
      "NotFoundException thrown" in new Test() {
        private val result = handler.onServerError(requestHeader, new NotFoundException("test") with NoStackTrace)
        status(result) shouldBe NOT_FOUND

        contentAsJson(result) shouldBe NotFoundError.asJson
      }
    }

    "return 401 with error body" when {
      "AuthorisationException thrown" in new Test() {
        private val result = handler.onServerError(requestHeader, new InsufficientEnrolments("test") with NoStackTrace)
        status(result) shouldBe UNAUTHORIZED

        contentAsJson(result) shouldBe UnAuthorisedError.asJson
      }
    }

    "return 500 with error body" when {
      "other exception thrown" in new Test() {
        private val result = handler.onServerError(requestHeader, new Exception with NoStackTrace)
        status(result) shouldBe INTERNAL_SERVER_ERROR

        contentAsJson(result) shouldBe InternalError.asJson
      }
    }
  }

}