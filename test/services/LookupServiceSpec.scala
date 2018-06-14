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

package services

import mocks.MockBusinessDetailsConnector
import models.errors._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LookupServiceSpec extends ServiceBaseSpec {

  trait Test extends MockBusinessDetailsConnector {
    lazy val target = new LookupService(mockBusinessDetailsConnector)
  }

  "calling .getMtdId" when {

    val nino: String = "AA123456A"

    "a known MTD NINO is passed" should {
      "return a valid mtdId" in new Test {

        val connectorResponse = Right("some id")

        mockGetMtdId(nino).returns(Future.successful(connectorResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe connectorResponse
      }
    }

    "a NotFoundError is returned" should {
      "transform the error into a forbidden error" in new Test {
        val connectorResponse = Left(NotFoundError)
        val serviceResponse = Left(ForbiddenError)

        mockGetMtdId(nino).returns(Future.successful(connectorResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    Map(
      "BadRequestError" -> BadRequestError,
      "ForbiddenError" -> ForbiddenError,
      "InternalServerError" -> InternalServerError,
      "ServiceUnavailableError" -> ServiceUnavailableError,
      "MalformedPayloadError" -> MalformedPayloadError
    ).foreach {
      case (description, error) =>
      s"a $description is returned" should {
        "transform the error into an internal server error" in new Test {

          val connectorResponse = Left(error)
          val serviceResponse = Left(InternalServerError)

          mockGetMtdId(nino).returns(Future.successful(connectorResponse))

          private val result = await(target.getMtdId(nino))

          result shouldBe serviceResponse
        }
      }
    }
  }
}
