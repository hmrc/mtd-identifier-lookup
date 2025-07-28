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
import models.errors.{InternalError, NotFoundError}
import models.outcomes.ResponseWrapper
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json, Reads}
import support.UnitSpec
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

// WLOG if Reads tested elsewhere
case class SomeModel(data: String)

object SomeModel {
  implicit val reads: Reads[SomeModel] = Json.reads
}

class StandardDownstreamHttpParserSpec extends UnitSpec with LogCapturing {

  private val method: String = "GET"
  private val url: String = "test-url"

  private val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  import connectors.httpParsers.StandardDownstreamHttpParser._

  private val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

  private val data: String = "someData"
  private val downstreamExpectedJson: JsValue = Json.obj("data" -> data)

  private val downstreamModel: SomeModel = SomeModel(data)
  private val downstreamResponse: ResponseWrapper[SomeModel] = ResponseWrapper(correlationId, downstreamModel)

  private val expectedSuccessLogMessage: String = s"[StandardDownstreamHttpParser][read] - " +
    s"Success response received with correlationId: $correlationId when calling $url"
  private def expectedFailureLogMessage(response: HttpResponse): String = s"[StandardDownstreamHttpParser][read] - " +
    s"Error response received with status: ${response.status} and body\n" +
    s"${response.body} and correlationId: $correlationId when calling $url"

  "The generic HTTP parser" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[SomeModel]] = implicitly

      "return a Right downstream response containing the model object if the response json corresponds to a model object" in {
        val httpResponse: HttpResponse = HttpResponse(OK, downstreamExpectedJson, Map("CorrelationId" -> Seq(correlationId)))

        withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
          httpReads.read(method, url, httpResponse) shouldBe Right(downstreamResponse)

          events.map(_.getMessage) should contain only expectedSuccessLogMessage
        }
      }

      "return an outbound error if a model object cannot be read from the response json" in {
        val badFieldTypeJson: JsValue = Json.obj("incomeSourceId" -> 1234, "incomeSourceName" -> 1234)
        val httpResponse: HttpResponse = HttpResponse(OK, badFieldTypeJson, Map("CorrelationId" -> Seq(correlationId)))
        val expected: ResponseWrapper[InternalError.type] = ResponseWrapper(correlationId, InternalError)

        withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
          httpReads.read(method, url, httpResponse) shouldBe Left(expected)

          events.map(_.getMessage) should contain(expectedSuccessLogMessage)
        }
      }
    }

    handleUnexpectedResponse(httpReads)
    handleHipErrorsCorrectly(httpReads)
  }

  "The generic HTTP parser for empty response" when {
    "no status code is specified" must {
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "receiving a 204 response" should {
        "return a Right DownstreamResponse with the correct correlationId and no responseData" in {
          val httpResponse: HttpResponse = HttpResponse(NO_CONTENT, "", Map("CorrelationId" -> Seq(correlationId)))

          withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
            httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))

            events.map(_.getMessage) should contain only expectedSuccessLogMessage
          }
        }
      }
    }

    "a success code is specified" must {
      implicit val successCode: SuccessCode             = SuccessCode(PARTIAL_CONTENT)
      val httpReads: HttpReads[DownstreamOutcome[Unit]] = implicitly

      "use that status code for success" in {
        val httpResponse: HttpResponse = HttpResponse(PARTIAL_CONTENT, "", Map("CorrelationId" -> Seq(correlationId)))

        withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
          httpReads.read(method, url, httpResponse) shouldBe Right(ResponseWrapper(correlationId, ()))

          events.map(_.getMessage) should contain only expectedSuccessLogMessage
        }
      }
    }

    handleUnexpectedResponse(httpReads)
    handleHipErrorsCorrectly(httpReads)
  }

  "validateJson" when {
    implicit val reads: Reads[SomeModel] = Json.reads[SomeModel]

    "the JSON is valid" should {
      "return the parsed model" in {
        val validJsonResponse: HttpResponse = HttpResponse(
          OK, downstreamExpectedJson, Map("CorrelationId" -> Seq(correlationId))
        )

        val result: Option[SomeModel] = validJsonResponse.validateJson[SomeModel]

        result shouldBe Some(downstreamModel)
      }
    }

    "the JSON is invalid" should {
      "return None" in {
        val invalidJsonResponse: HttpResponse = HttpResponse(
          OK, Json.obj("data" -> 1234), Map("CorrelationId" -> Seq(correlationId))
        )

        val result: Option[SomeModel] = invalidJsonResponse.validateJson[SomeModel]

        result shouldBe None
      }
    }

    "the response contains no JSON" should {
      "return None" in {
        val emptyResponse: HttpResponse = HttpResponse(OK, "", Map("CorrelationId" -> Seq(correlationId)))

        val result: Option[SomeModel] = emptyResponse.validateJson[SomeModel]

        result shouldBe None
      }
    }
  }

  private def handleUnexpectedResponse[A](httpReads: HttpReads[DownstreamOutcome[A]]): Unit =
    Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { responseCode =>
      s"receiving an unexpected $responseCode response" should {
        "return an outbound error for an incorrectly formatted error" in {
          val httpResponse: HttpResponse = HttpResponse(responseCode, JsObject.empty, Map("CorrelationId" -> Seq(correlationId)))

          withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
            httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, InternalError))

            events.map(_.getMessage) should contain only expectedFailureLogMessage(httpResponse)
          }
        }
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
          val httpResponse: HttpResponse = HttpResponse(
            UNPROCESSABLE_ENTITY,
            singleHipErrorJson(code),
            Map("CorrelationId" -> List(correlationId))
          )

          withCaptureOfLoggingFrom(StandardDownstreamHttpParser.logger) { events =>
            httpReads.read(method, url, httpResponse) shouldBe Left(ResponseWrapper(correlationId, expectedError))

            events.map(_.getMessage) should contain only expectedFailureLogMessage(httpResponse)
          }
        }
      }
    }
  }
}
