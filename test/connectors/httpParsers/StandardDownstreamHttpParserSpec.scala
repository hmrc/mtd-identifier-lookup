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
import models.errors.{ForbiddenError, InternalError, NotFoundError}
import models.outcomes.ResponseWrapper
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Reads}
import support.UnitSpec
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

// WLOG if Reads tested elsewhere
case class SomeModel(data: String)

object SomeModel {
  implicit val reads: Reads[SomeModel] = Json.reads
}

class StandardDownstreamHttpParserSpec extends UnitSpec {

  val method = "GET"
  val url    = "test-url"

  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  import connectors.httpParsers.StandardDownstreamHttpParser._

  val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

  val data                            = "someData"
  val downstreamExpectedJson: JsValue = Json.obj("data" -> data)

  val downstreamModel: SomeModel                     = SomeModel(data)
  val downstreamResponse: ResponseWrapper[SomeModel] = ResponseWrapper(correlationId, downstreamModel)

  "The generic HTTP parser" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[SomeModel]] = implicitly

      "return a Right downstream response containing the model object if the response json corresponds to a model object" in {
        val httpResponse = HttpResponse(OK, downstreamExpectedJson, Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(downstreamResponse)
      }

      "return an outbound error if a model object cannot be read from the response json" in {
        val badFieldTypeJson: JsValue = Json.obj("incomeSourceId" -> 1234, "incomeSourceName" -> 1234)
        val httpResponse              = HttpResponse(OK, badFieldTypeJson, Map("CorrelationId" -> Seq(correlationId)))
        val expected                  = ResponseWrapper(correlationId, InternalError)

        httpReads.read(method, url, httpResponse) shouldBe Left(expected)
      }
    }

    handleErrorsCorrectly(httpReads)
    handleInternalErrorsCorrectly(httpReads)
    handleUnexpectedResponse(httpReads)
    handleHipErrorsCorrectly(httpReads)
  }

  "The generic HTTP parser for empty response" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "receiving a 204 response" should {
        "return a Right DownstreamResponse with the correct correlationId and no responseData" in {
          val httpResponse = HttpResponse(NO_CONTENT, "", Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
        }
      }
    }

    "a success code is specified" must {
      implicit val successCode: SuccessCode             = SuccessCode(PARTIAL_CONTENT)
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "use that status code for success" in {
        val httpResponse = HttpResponse(PARTIAL_CONTENT, "", Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    handleErrorsCorrectly(httpReads)
    handleInternalErrorsCorrectly(httpReads)
    handleUnexpectedResponse(httpReads)
    handleHipErrorsCorrectly(httpReads)
  }

  "validateJson" when {
    implicit val reads: Reads[SomeModel] = Json.reads[SomeModel]

    "the JSON is valid" should {
      "return the parsed model" in {
        val validJsonResponse = HttpResponse(OK, Json.obj("data" -> "someData"), Map("CorrelationId" -> Seq(correlationId)))

        val result = validJsonResponse.validateJson[SomeModel]

        result shouldBe Some(SomeModel("someData"))
      }
    }

    "the JSON is invalid" should {
      "return None" in {
        val invalidJsonResponse = HttpResponse(OK, Json.obj("data" -> 1234), Map("CorrelationId" -> Seq(correlationId)))

        val result = invalidJsonResponse.validateJson[SomeModel]

        result shouldBe None
      }
    }

    "the response contains no JSON" should {
      "return None" in {
        val emptyResponse = HttpResponse(OK, "", Map("CorrelationId" -> Seq(correlationId)))

        val result = emptyResponse.validateJson[SomeModel]

        result shouldBe None
      }
    }
  }

  val singleErrorJson: JsValue = Json.parse(
    """
      |{
      |   "code": "CODE",
      |   "reason": "MESSAGE"
      |}
    """.stripMargin
  )

  private def handleErrorsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    Map(BAD_REQUEST -> InternalError, NOT_FOUND -> NotFoundError, FORBIDDEN -> ForbiddenError).foreach(responseCode =>
      s"receiving a $responseCode response" should {
        "be able to parse a single error" in {
          val httpResponse = HttpResponse(responseCode._1, singleErrorJson, Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, responseCode._2))
        }
      })

  private def handleInternalErrorsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach(responseCode =>
      s"receiving a $responseCode response" should {
        "return an outbound error when the error returned matches the Error model" in {
          val httpResponse = HttpResponse(responseCode, singleErrorJson, Map("CorrelationId" -> Seq(correlationId)))

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, InternalError))
        }
      })

  private def handleUnexpectedResponse[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    "receiving an unexpected response" should {
      val responseCode = 499
      "return an outbound error when the error returned matches the Error model" in {
        val httpResponse = HttpResponse(responseCode, singleErrorJson, Map("CorrelationId" -> Seq(correlationId)))

        httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, InternalError))
      }

    }

  def singleHipErrorJson(code: String): JsValue = Json.parse(
    s"""
      |{
      |  "errors": {
      |    "processingDate": "2024-07-15T09:45:17Z",
      |    "code": "$code",
      |    "text": "some text"
      |  }
      |}
    """.stripMargin
  )

  private def handleHipErrorsCorrectly[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit = {
    Seq(
      ("001", InternalError),
      ("006", NotFoundError),
      ("007", InternalError),
      ("008", NotFoundError)
    ).foreach { case (code, expectedError) =>
      s"receiving a 422 response with an errors object containing code $code" should {
        s"return a Left ResponseWrapper containing $expectedError" in {
          val httpResponse = HttpResponse(
            UNPROCESSABLE_ENTITY,
            singleHipErrorJson(code),
            Map("CorrelationId" -> List(correlationId))
          )

          httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, expectedError))
        }
      }
    }
  }
}
