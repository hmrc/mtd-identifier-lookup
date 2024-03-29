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

package controllers

import mocks.{MockAuthService, MockLookupService}
import models.MtdIdResponse
import models.errors._
import play.api.libs.json.Json

import scala.concurrent.Future

class LookupControllerSpec extends ControllerBaseSpec {

  private trait Test extends MockAuthService with MockLookupService {
    lazy val target: LookupController = new LookupController(mockAuthService, mockLookupService, cc)(ec, idGenerator)

  }


  val nino: String                   = "AA123456A"
  val mtdId: String                  = "1234567890"
  val reference: MtdIdResponse       = MtdIdResponse(mtdId)
  implicit val correlationId: String = idGenerator.generateCorrelationId


  "Calling lookup with a known NINO" should {
    "return 200 (OK)" in new Test {
      authoriseUser()

      MockLookupService.getMtdId(nino).returns(Future.successful(Right(reference)))
      private val result = target.lookup(nino)(fakeRequest)
      status(result) shouldBe OK
    }

    "return valid json" in new Test {
      authoriseUser()

      private val expectedResponse = Json.parse(
        s"""
           |{
           |   "mtdbsa":"$mtdId"
           |}
          """.stripMargin
      )

      MockLookupService.getMtdId(nino).returns(Future.successful(Right(reference)))
      private val result = target.lookup(nino)(fakeRequest)
      contentAsJson(result) shouldBe expectedResponse
    }
  }

  "Calling lookup with a non-MTD NINO" should {
    "return 403 (Forbidden)" in new Test {
      authoriseUser()

      MockLookupService.getMtdId(nino).returns(Future.successful(Left(ForbiddenError)))
      private val result = target.lookup(nino)(fakeRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

  "Calling lookup and an error occurs" should {
    "return 500 (Internal server error)" in new Test {
      authoriseUser()

      MockLookupService.getMtdId(nino).returns(Future.successful(Left(InternalError)))
      private val result = target.lookup(nino)(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling lookup with an invalid NINO" should {
    "return 400 (Bad Request)" in new Test {
      authoriseUser()
      private val result = target.lookup("BOB")(fakeRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

}
