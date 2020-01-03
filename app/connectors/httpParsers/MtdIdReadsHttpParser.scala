/*
 * Copyright 2020 HM Revenue & Customs
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

import models.errors._
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object MtdIdReadsHttpParser {

  implicit val reader: HttpReads[Either[ExternalServiceError, String]] = new HttpReads[Either[ExternalServiceError, String]] {

    private def extractId(json: JsValue): Either[ExternalServiceError, String] = {
      (json \ "mtdbsa").validate[String] match {
        case success: JsSuccess[String] => Right(success.value)
        case error: JsError =>
          Logger.warn(s"[MtdIdReadsHttpParser][read]: ${error.toString}")
          Left(MalformedPayloadError)
      }
    }

    override def read(method: String, url: String, response: HttpResponse): Either[ExternalServiceError, String] = {
      response.status match {
        case Status.OK => extractId(response.json)
        case Status.BAD_REQUEST =>
          Logger.warn(s"[MtdIdReadsHttpParser][read]: Bad Request from DES: \nBody - ${response.body}\nURl - $url")
          Left(BadRequestError)
        case Status.NOT_FOUND =>
          Logger.warn(s"[MtdIdReadsHttpParser][read]: Not Found from DES: \nBody - ${response.body}\nURl - $url")
          Left(NotFoundError)
        case Status.SERVICE_UNAVAILABLE =>
          Logger.warn(s"[MtdIdReadsHttpParser][read]: Service Unavailable from DES: \nBody - ${response.body}\nURl - $url")
          Left(ServiceUnavailableError)
        case Status.INTERNAL_SERVER_ERROR =>
          Logger.warn(s"[MtdIdReadsHttpParser][read]: ISE from DES: \nBody - ${response.body}\nURl - $url")
          Left(InternalServerError)
      }
    }
  }

}
