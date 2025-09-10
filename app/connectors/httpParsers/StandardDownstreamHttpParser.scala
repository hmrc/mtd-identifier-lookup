/*
 * Copyright 2025 HM Revenue & Customs
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

import models.connectors.DownstreamOutcome
import models.errors.{InternalError, MtdError, NotFoundError}
import models.outcomes.ResponseWrapper
import play.api.http.Status.*
import play.api.libs.json.{JsObject, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object StandardDownstreamHttpParser extends HttpParser {

  case class SuccessCode(status: Int) extends AnyVal

  implicit def readsEmpty(implicit successCode: SuccessCode = SuccessCode(NO_CONTENT)): HttpReads[DownstreamOutcome[Unit]] =
    (_: String, url: String, response: HttpResponse) => doRead(url, response, () => Right(ResponseWrapper(retrieveCorrelationId(response), ())))

  implicit def reads[A: Reads](implicit successCode: SuccessCode = SuccessCode(OK)): HttpReads[DownstreamOutcome[A]] =
    (_: String, url: String, response: HttpResponse) =>
      doRead(
        url,
        response,
        () => {
          val correlationId = retrieveCorrelationId(response)
          response.validateJson[A] match {
            case Some(ref) => Right(ResponseWrapper(correlationId, ref))
            case None      => Left(ResponseWrapper(correlationId, InternalError))
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
    else { handleErrorResponse(correlationId, response.status, response) }
  }

  private def extractErrorCode(response: HttpResponse): Option[String] =
    response.validateJson[JsObject].flatMap { jsonObject =>
      response.parseResult((jsonObject \ "errors" \ "code").validate[String])
    }

  private def handleErrorResponse(correlationId: String, status: Int, response: HttpResponse): DownstreamOutcome[Nothing] = {
    val error: MtdError = status match {
      case UNPROCESSABLE_ENTITY =>
        extractErrorCode(response) match {
          case Some("006") | Some("008") => NotFoundError
          case _                         => InternalError
        }
      case _ => InternalError
    }

    Left(ResponseWrapper(correlationId, error))
  }

}
