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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuthStub, BusinessDetailsStub}
import support.IntegrationBaseSpec

class LookupControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    def setupStubs(): StubMapping

    def request(nino: String): WSRequest = {
      setupStubs()
      buildRequest(s"/nino/$nino")
    }
  }

  "Calling the mtd lookup endpoint" when {

    val nino = "AA123456A"

    "the user is authorised" should {

      "return 200" in new Test {
        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          BusinessDetailsStub.getMtdId(Json.obj("mtdbsa" -> "1234567890").toString(), nino, Status.OK)
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
          BusinessDetailsStub.getMtdId("", nino, Status.NOT_FOUND)
        }

        val response: WSResponse = await(request(nino).get())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }
}
