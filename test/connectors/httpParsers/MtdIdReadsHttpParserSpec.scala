/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse

class MtdIdReadsHttpParserSpec extends UnitSpec {

  private trait Test {
    val body: Option[JsValue]
    val statusCode: Int
    lazy val httpResponse = HttpResponse(statusCode, body)
    lazy val result: Either[ExternalServiceError, String] = MtdIdReadsHttpParser.reader.read("", "", httpResponse)
  }

  "MtdIdReadsHttpParser.reader" when {
    "the http response status is 200 OK with a valid body" should {

      "return a MTD ID" in new Test {
        override val body = Some(Json.parse("""{"mtdbas": "1234567890"}"""))
        override val statusCode: Int = Status.OK

        result shouldBe Right("1234567890")
      }
    }

    "the http response status is 200 OK with an invalid body" should {

      "return a MalformedPayloadError" in new Test {
        override val body = Some(Json.parse("""{"mtdbas": 1234}"""))
        override val statusCode: Int = Status.OK

        result shouldBe Left(MalformedPayloadError)
      }
    }

    "the http response status is 400 (bad request)" should {

      "return a BadRequestError" in new Test {
        override val body: None.type = None
        override val statusCode: Int = Status.BAD_REQUEST

        result shouldBe Left(BadRequestError)
      }
    }

    "the http response status is 404 (not found)" should {

      "return a NotFoundError" in new Test {
        override val body: None.type = None
        override val statusCode: Int = Status.NOT_FOUND

        result shouldBe Left(NotFoundError)
      }
    }

    "the http response status is 500 (internal server error)" should {

      "return a InternalServerError" in new Test {
        override val body: None.type = None
        override val statusCode: Int = Status.INTERNAL_SERVER_ERROR

        result shouldBe Left(InternalServerError)
      }
    }

    "the http response status is 503 (service unavailable)" should {

      "return a ServiceUnavailableError" in new Test {
        override val body: None.type = None
        override val statusCode: Int = Status.SERVICE_UNAVAILABLE

        result shouldBe Left(ServiceUnavailableError)
      }
    }

  }
}
