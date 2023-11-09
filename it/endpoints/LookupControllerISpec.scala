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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.domain.MtdIdReference
import play.api.libs.json.Json
import play.api.http.Status
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}
import stubs.{AuthStub, DownstreamStub}
import support.IntegrationBaseSpec

class LookupControllerISpec extends IntegrationBaseSpec {

  val nino      = "AA123456A"
  val mtdId     = "1234567890"
  val reference = MtdIdReference(mtdId)

  private val ifsJson = Json.parse(
    """
      |{
      |   "taxPayerDisplayResponse": {
      |       "nino": "NS112233A",
      |       "mtdId": "XAIT0000000000"
      |    }
      |}
  """.stripMargin
  )

  private trait Test {
    def setupStubs(): StubMapping

    def request(nino: String): WSRequest = {
      setupStubs()
      buildRequest(s"/nino/$nino")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the mtd lookup endpoint" when {

    "the user is authorised" should {

      "return 200" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, s"/registration/business-details/nino/$nino", Status.OK, ifsJson)
        }

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.OK
      }
    }

    "the user is NOT logged in" should {

      "return 401" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.UNAUTHORIZED
      }
    }

    "the user is NOT authorised" should {

      "return 401" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.UNAUTHORIZED
      }
    }

    "the user is authorised but non-MTD nino" should {

      "return 403" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          repository.removeAll()
          DownstreamStub.onError(DownstreamStub.GET, s"/registration/business-details/nino/$nino", Status.NOT_FOUND, errorBody("NOT_FOUND"))
        }
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
