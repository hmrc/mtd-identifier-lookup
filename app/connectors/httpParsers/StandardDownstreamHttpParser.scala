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

package connectors.httpParsers

import connectors.httpParsers.HttpParser
import models.connectors.DownstreamOutcome
import models.errors.{ForbiddenError, InternalError, NotFoundError}
import models.outcomes.ResponseWrapper
import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StandardDownstreamHttpParser extends HttpParser {

  case class SuccessCode(status: Int) extends AnyVal

  implicit def readsEmpty(implicit successCode: SuccessCode = SuccessCode(NO_CONTENT)): HttpReads[DownstreamOutcome[Unit]] =
    (_: String, url: String, response: HttpResponse) => doRead(url, response, () => Right(ResponseWrapper(retrieveCorrelationId(response), ())))

  import play.api.http.Status._

  implicit def reads[A: Reads](implicit successCode: SuccessCode = SuccessCode(OK)): HttpReads[DownstreamOutcome[A]] =
    (_: String, url: String, response: HttpResponse) =>
      doRead(
        url,
        response,
        () => {
          val correlationId = retrieveCorrelationId(response)
          if (response.status == OK) {
            response.validateJson[A] match {
              case Some(ref) => Right(ResponseWrapper(correlationId, ref))
              case None      => Left(ResponseWrapper(correlationId, InternalError))
            }
          } else {
            handleErrorResponse(correlationId, response.status)
          }
        }
      )

  private def doRead[A](url: String, response: HttpResponse, successOutcomeFactory: () => DownstreamOutcome[A])(implicit
      successCode: SuccessCode): DownstreamOutcome[A] = {

    val correlationId = retrieveCorrelationId(response)
    val success       = response.status == successCode.status

    if (!success) {
      logger.warn(
        s"[StandardDownstreamHttpParser][read] - " +
          s"Error response received with status: ${response.status} and body\n" +
          s"${response.body} and correlationId: $correlationId when calling $url")
    } else {
      logger.info(
        s"[StandardDownstreamHttpParser][read] - " +
          s"Success response received with correlationId: $correlationId when calling $url")
    }

    if (success) { successOutcomeFactory() }
    else { handleErrorResponse(correlationId, response.status) }
  }

  private def handleErrorResponse(correlationId: String, status: Int): DownstreamOutcome[Nothing] = {
    status match {
      case NOT_FOUND => Left(ResponseWrapper(correlationId, NotFoundError))
      case FORBIDDEN => Left(ResponseWrapper(correlationId, ForbiddenError))
      case _         => Left(ResponseWrapper(correlationId, InternalError))
    }
  }

}
