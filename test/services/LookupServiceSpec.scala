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

package services

import mocks.{MockAppConfig, MockBusinessDetailsConnector, MockLookupRepository, MockNinoHasher, MockTimeProvider}
import models.{MtdIdCached, MtdIdReference, MtdIdResponse}
import models.errors._
import models.outcomes.ResponseWrapper
import play.api.Configuration
import uk.gov.hmrc.crypto.{PlainText, Scrambled}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.Instant
import scala.concurrent.Future

class LookupServiceSpec extends ServiceBaseSpec with MockAppConfig {

  val ninoHash: String = "hashed-nino-value"
  val nino: String = "AA123456A"
  val mtdId: String = "some id"
  val reference: MtdIdReference = MtdIdReference(mtdId)
  val mtdIdResponse: MtdIdResponse = MtdIdResponse(mtdId)
  val fixedInstant: Instant = Instant.parse("2025-01-02T00:00:00.000Z")
  val cached: MtdIdCached = MtdIdCached(
    ninoHash = ninoHash,
    nino = SensitiveString(nino),
    mtdRef = SensitiveString(mtdId),
    lastUpdated = fixedInstant
  )
  val lookupCacheResponse: Option[MtdIdCached] = None
  val isCachedResponse: Boolean = true

  trait Test extends MockBusinessDetailsConnector with MockLookupRepository with MockTimeProvider with MockNinoHasher {
    lazy val target = new LookupService(
      connector = mockBusinessDetailsConnector,
      repository = mockLookupRepository,
      appConfig = mockAppConfig,
      timeProvider = mockTimeProvider,
      ninoHasher = mockNinoHasher
    )
  }

  "calling .getMtdId function" when {
    "a known MTD NINO is passed and not available in lookup cache" should {
      "save a valid mtdId in the lookup cache and return the mtdId" in new Test {
        MockedAppConfig.featureSwitches
          .returns(Configuration("mongo-lookup.enabled" -> true))
          .anyNumberOfTimes()

        MockNinoHasher.hash(PlainText(nino)).returns(Scrambled(ninoHash)).twice()
        MockedLookupRepository.getMtdReference(ninoHash).returns(Future.successful(lookupCacheResponse))
        mockGetMtdId(nino).returns(Future.successful(Right(ResponseWrapper(correlationId, reference))))
        MockedLookupRepository.save(cached).returns(Future.successful(isCachedResponse))
        MockTimeProvider.now().returns(fixedInstant)

        private val result: Either[MtdError, MtdIdResponse] = await(target.getMtdId(nino))

        result shouldBe Right(mtdIdResponse)
      }
    }

    "a known MTD NINO is passed which is available in lookup cache" should {
      "return the mtdId from the lookup cache" in new Test {
        val lookupCacheResponse: Option[MtdIdCached] = Some(cached)
        val serviceResponse: Right[Nothing, MtdIdResponse] = Right(mtdIdResponse)

        MockedAppConfig.featureSwitches
          .returns(Configuration("mongo-lookup.enabled" -> true))
          .anyNumberOfTimes()

        MockNinoHasher.hash(PlainText(nino)).returns(Scrambled(ninoHash))
        MockedLookupRepository.getMtdReference(ninoHash).returns(Future.successful(lookupCacheResponse))
        mockGetMtdId(nino).never()

        private val result: Either[MtdError, MtdIdResponse] = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    "the mongo lookup repository is switched off" should {
      "call downstream and return the mtdId" in new Test {
        val serviceResponse: Right[Nothing, MtdIdResponse] = Right(mtdIdResponse)

        MockedAppConfig.featureSwitches
          .returns(Configuration("mongo-lookup.enabled" -> false))
          .anyNumberOfTimes()

        MockNinoHasher.hash(PlainText(nino)).returns(Scrambled(ninoHash)).never()
        MockedLookupRepository.getMtdReference(ninoHash).never()
        mockGetMtdId(nino).returns(Future.successful(Right(ResponseWrapper(correlationId, reference))))
        MockedLookupRepository.save(cached).never()
        MockTimeProvider.now().returns(fixedInstant).never()

        private val result: Either[MtdError, MtdIdResponse] = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    "a NotFoundError is returned" should {
      "transform the error into a forbidden error" in new Test {
        val serviceResponse: Left[ForbiddenError.type, Nothing] = Left(ForbiddenError)
        val lookupRepositoryResponse: Option[Nothing] = None

        MockedAppConfig.featureSwitches
          .returns(Configuration("mongo-lookup.enabled" -> true))
          .anyNumberOfTimes()

        MockNinoHasher.hash(PlainText(nino)).returns(Scrambled(ninoHash))
        MockedLookupRepository.getMtdReference(ninoHash).returns(Future.successful(lookupRepositoryResponse))
        mockGetMtdId(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, NotFoundError))))

        private val result: Either[MtdError, MtdIdResponse] = await(target.getMtdId(nino))

        result shouldBe serviceResponse
      }
    }

    Seq(
      NinoFormatError,
      ForbiddenError,
      InternalError,
      UnAuthorisedError
    ).foreach { error =>
      s"a ${error.code} is returned" should {
        "transform the error into an internal server error" in new Test {
          val serviceResponse: Left[InternalError.type, Nothing] = Left(InternalError)
          val lookupRepositoryResponse: Option[Nothing] = None

          MockedAppConfig.featureSwitches
            .returns(Configuration("mongo-lookup.enabled" -> true))
            .anyNumberOfTimes()

          MockNinoHasher.hash(PlainText(nino)).returns(Scrambled(ninoHash))
          MockedLookupRepository.getMtdReference(ninoHash).returns(Future.successful(lookupRepositoryResponse))
          mockGetMtdId(nino).returns(Future.successful(Left(ResponseWrapper(correlationId, error))))

          private val result: Either[MtdError, MtdIdResponse] = await(target.getMtdId(nino))

          result shouldBe serviceResponse
        }
      }
    }
  }
}
