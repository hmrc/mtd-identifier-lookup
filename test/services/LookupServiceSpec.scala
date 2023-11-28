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

import mocks.{MockAppConfig, MockBusinessDetailsConnector, MockLookupRepository}
import models.{MtdIdCached, MtdIdDesReference, MtdIdIfsReference, MtdIdResponse}
import models.errors._
import models.outcomes.ResponseWrapper
import play.api.Configuration

import scala.concurrent.Future

class LookupServiceSpec extends ServiceBaseSpec with MockAppConfig {

  val nino: String                             = "AA123456A"
  val mtdId                                    = "some id"
  val ifsReference: MtdIdIfsReference          = MtdIdIfsReference(mtdId)
  val desReference: MtdIdDesReference          = MtdIdDesReference(mtdId)
  val reference: MtdIdResponse                 = MtdIdResponse(mtdId)
  val cached: MtdIdCached                      = MtdIdCached(nino, mtdId)
  val lookupCacheResponse: Option[MtdIdCached] = None
  val isCachedResponse: Boolean                = true

  trait Test extends MockBusinessDetailsConnector with MockLookupRepository {
    lazy val target = new LookupService(mockBusinessDetailsConnector, mockLookupRepository, mockAppConfig)

  }

  "calling .getMtdIdFromDes function" when {
    "a known MTD Nino is passed" should {
      "return a valid mtdId" in new Test {
        MockedAppConfig.featureSwitches
          .returns(Configuration("ifs.enabled" -> false))
          .anyNumberOfTimes()

        mockGetMtdIdFromDes(nino).returns(Future.successful(Right(ResponseWrapper(correlationId, desReference))))
        MockedLookupRepository.save(cached).returns(Future.successful(isCachedResponse))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe Right(reference)
      }
    }
  }

  "calling .getMtdIdFromIfs function" when {

    "a known MTD NINO is passed and not available in lookup cache" should {
      "save a valid mtdId in the lookup cache and return" in new Test {

        MockedAppConfig.featureSwitches
          .returns(Configuration("ifs.enabled" -> true))
          .anyNumberOfTimes()

        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))
        mockGetMtdIdFromIfs(nino).returns(Future.successful(Right(ResponseWrapper(correlationId, ifsReference))))
        MockedLookupRepository.save(cached).returns(Future.successful(isCachedResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe Right(reference)
      }
    }

    "a known MTD NINO is passed which is available in lookup cache" should {
      "return the mtdId from the lookup cache" in new Test {

        val lookupCacheResponse: Option[MtdIdCached] = Some(cached)
        val serviceResponse                          = Right(reference)

        mockGetMtdIdFromIfs(nino).never()
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupCacheResponse))

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    "a NotFoundError is returned" should {
      "transform the error into a forbidden error" in new Test {
        val serviceResponse          = Left(ForbiddenError)
        val lookupRepositoryResponse = None
        mockGetMtdIdFromIfs(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, NotFoundError))))
        MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))
        MockedAppConfig.featureSwitches
          .returns(Configuration("ifs.enabled" -> true))
          .anyNumberOfTimes()

        private val result = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    Seq(
      NinoFormatError,
      ForbiddenError,
      InternalError,
      UnAuthorisedError
    ).foreach { error =>
      s"a ${error.message} is returned" should {
        "transform the error into an internal server error" in new Test {

          val serviceResponse          = Left(InternalError)
          val lookupRepositoryResponse = None
          MockedAppConfig.featureSwitches
            .returns(Configuration("ifs.enabled" -> true))
            .anyNumberOfTimes()

          mockGetMtdIdFromIfs(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, error))))

          MockedLookupRepository.getMtdReference(nino).returns(Future.successful(lookupRepositoryResponse))

          private val result = await(target.getMtdId(nino))

          result shouldBe serviceResponse
        }
      }
    }
  }

}
