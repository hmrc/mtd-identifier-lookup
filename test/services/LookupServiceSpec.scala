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

package services

import mocks.{MockBusinessDetailsConnector, MockLookupRepository}
import models.MtdIdReference
import models.errors._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LookupServiceSpec extends ServiceBaseSpec {

  trait Test extends MockBusinessDetailsConnector with MockLookupRepository{
    lazy val target = new LookupService(mockBusinessDetailsConnector, mockLookupRepository)
  }

  "calling .getMtdId function" when {

    val nino: String = "AA123456A"
    val mtdId = "some id"
    "a known MTD NINO is passed and not available in lookup cache" should {
      "save a valid mtdId in the lookup cache and return" in new Test {

        val connectorResponse = Right(mtdId)
        val lookupCacheResponse = None
        val isCachedResponse = true

        MockedLookupRepository.save(nino, mtdId).returns(Future.successful(isCachedResponse))
        mockGetMtdId(nino).returns(Future.successful(connectorResponse))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe connectorResponse
      }
    }

    "a known MTD NINO is passed which is available in lookup cache" should {
      "return the mtdId from the lookup cache" in new Test {

        val lookupCacheResponse = Some(MtdIdReference(nino, mtdId))
        val serviceResponse = Right(mtdId)

        mockGetMtdId(nino).never()
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    "a NotFoundError is returned" should {
      "transform the error into a forbidden error" in new Test {
        val connectorResponse = Left(NotFoundError)
        val serviceResponse = Left(ForbiddenError)
        val lookupRepositoryResponse = None

        mockGetMtdId(nino).returns(Future.successful(connectorResponse))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))

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
          val lookupRepositoryResponse = None

          mockGetMtdId(nino).returns(Future.successful(connectorResponse))
          MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))

          private val result = await(target.getMtdId(nino))

          result shouldBe serviceResponse
        }
      }
    }
  }
}
