/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}
import stubs.{AuthStub, DownstreamStub}
import support.IntegrationBaseSpec

trait BaseControllerISpec extends IntegrationBaseSpec {
  val nino: String
  val connectorResponseJson: JsValue
  val responseJson: JsValue
  val reference: MtdIdResponse

  def servicesConfig: Map[String, Any] = Map(
    "microservice.services.des.host"  -> mockHost,
    "microservice.services.des.port"  -> mockPort,
    "microservice.services.ifs.host"  -> mockHost,
    "microservice.services.ifs.port"  -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "auditing.consumer.baseUri.port"  -> mockPort,
    "feature-switch.ifs.enabled"      -> false
  )

  def request(nino: String): WSRequest = {
    buildRequest(s"/nino/$nino")
      .withHttpHeaders(
        (ACCEPT, "application/vnd.hmrc.1.0+json"),
        (AUTHORIZATION, "Bearer 123")
      )
  }

  def responseSuccessful(): Unit = {
    "the user is authorised" should {
      "return 200" in {
        AuthStub.authorised()
        DownstreamStub.onSuccess(DownstreamStub.GET, s"/registration/business-details/nino/$nino", Status.OK, connectorResponseJson)
        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.OK
        response.json shouldBe responseJson
      }
    }
  }

  def notLoggedIn(): Unit = {
    "the user is NOT logged in" should {
      "return 401" in {
        AuthStub.unauthorisedNotLoggedIn()
        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.UNAUTHORIZED
      }
    }
  }

  def unauthorised(): Unit = {
    "the user is NOT authorised" should {
      "return 401" in {
        AuthStub.unauthorisedOther()

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.UNAUTHORIZED
      }
    }
  }

  def nonMtdNino(): Unit = {
    "the user is authorised but non-MTD nino" should {
      "return 403" in {
        AuthStub.authorised()
        repository.removeAll()
        DownstreamStub.onError(DownstreamStub.GET, s"/registration/business-details/nino/$nino", Status.NOT_FOUND, errorBody("NOT_FOUND"))

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }

  def errorBody(code: String): String =
    s"""
       |{
       |  "code": "$code",
       |  "reason": "error message from downstream"
       |}
     """.stripMargin

}
