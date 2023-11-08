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

package services

import mocks.{MockBusinessDetailsConnector, MockLookupRepository}
import models.domain.{MtdIdMongoReference, MtdIdReference}
import models.errors._
import models.outcomes.ResponseWrapper

import scala.concurrent.Future

class LookupServiceSpec extends ServiceBaseSpec {

  val nino: String = "AA123456A"
  val mtdId = "some id"
  val reference: MtdIdReference = MtdIdReference(mtdId)
  val cached: MtdIdMongoReference = MtdIdMongoReference(nino, mtdId)
  trait Test extends MockBusinessDetailsConnector with MockLookupRepository {
    lazy val target = new LookupService(mockBusinessDetailsConnector, mockLookupRepository)
  }

  "calling .getMtdId function" when {


    "a known MTD NINO is passed and not available in lookup cache" should {
      "save a valid mtdId in the lookup cache and return" in new Test {

        val connectorResponse   = Right(reference)
        val lookupCacheResponse = None
        val isCachedResponse    = true

        mockGetMtdId(nino).returns(Future.successful(Right(ResponseWrapper(correlationId, reference))))
        MockedLookupRepository.save(cached).returns(Future.successful(isCachedResponse))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe connectorResponse
      }
    }

    "a known MTD NINO is passed which is available in lookup cache" should {
      "return the mtdId from the lookup cache" in new Test {

        val lookupCacheResponse = Some(cached)
        val serviceResponse     = Right(reference)

        mockGetMtdId(nino).never()
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    "a NotFoundError is returned" should {
      "transform the error into a forbidden error" in new Test {
        val serviceResponse          = Left(ForbiddenError)
        val lookupRepositoryResponse = None
        mockGetMtdId(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, NotFoundError))))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    Map(
      "BadRequestError"         -> NinoFormatError,
      "ForbiddenError"          -> ForbiddenError,
      "InternalServerError"     -> InternalError,
      "ServiceUnavailableError" -> UnauthorisedError
    ).foreach { case (description, error) =>
      s"a $description is returned" should {
        "transform the error into an internal server error" in new Test {

          val serviceResponse          = Left(InternalError)
          val lookupRepositoryResponse = None

          mockGetMtdId(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, error))))

          MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))

          private val result = await(target.getMtdId(nino))

          result shouldBe serviceResponse
        }
      }
    }
  }

}
