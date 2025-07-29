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

package endpoints

import models.MtdIdResponse
import models.errors.MtdError
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION, OK, UNAUTHORIZED}
import stubs.{AuthStub, DownstreamStub}
import support.IntegrationBaseSpec

trait BaseControllerISpec extends IntegrationBaseSpec {

  val downstreamResponseJson: JsValue
  val downstreamUrl: String
  val downstreamQueryParam: Map[String, String]

  val nino: String = "AA123456A"
  val mtdId: String = "1234567890"
  val reference: MtdIdResponse = MtdIdResponse(mtdId)

  val responseJson: JsValue = Json.parse(
    s"""
      |{
      |  "mtdbsa": "$mtdId"
      |}
    """.stripMargin
  )

  def servicesConfig: Map[String, Any] = Map(
    "microservice.services.hip.host"  -> mockHost,
    "microservice.services.hip.port"  -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "auditing.consumer.baseUri.port"  -> mockPort
  )

  def request(nino: String): WSRequest = {
    buildRequest(s"/nino/$nino")
      .withHttpHeaders(
        (ACCEPT, "application/vnd.hmrc.1.0+json"),
        (AUTHORIZATION, "Bearer 123")
      )
  }

  def responseSuccessful(): Unit =
    "the user is authorised" should {
      "return 200" in {
        AuthStub.authorised()
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, downstreamQueryParam, OK, downstreamResponseJson)

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe OK
        response.json shouldBe responseJson
      }
    }

  def notLoggedIn(): Unit =
    "the user is NOT logged in" should {
      "return 401" in {
        AuthStub.unauthorisedNotLoggedIn()

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe UNAUTHORIZED
      }
    }

  def unauthorised(): Unit =
    "the user is NOT authorised" should {
      "return 401" in {
        AuthStub.unauthorisedOther()

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe UNAUTHORIZED
      }
    }

  def responseFailures(downstreamStatus: Int,
                       downstreamCode: String,
                       errorBody: String,
                       expectedStatus: Int,
                       expectedBody: MtdError): Unit =
    s"the user is authorised but downstream returns a code $downstreamCode error and status $downstreamStatus" should {
      s"return the expected status $expectedStatus and error $expectedBody according to spec" in {
        AuthStub.authorised()
        DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, downstreamQueryParam, downstreamStatus, errorBody)

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }
}
