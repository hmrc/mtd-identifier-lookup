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

import com.google.inject.{Inject, Singleton}
import config.{AppConfig, FeatureSwitches}
import connectors.BusinessDetailsConnector
import hasher.NinoHasher
import models.connectors.DownstreamOutcome
import models.errors.{ForbiddenError, InternalError, MtdError, NotFoundError}
import models.outcomes.ResponseWrapper
import models._
import repositories.LookupRepository
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.http.HeaderCarrier
import utils.TimeProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupService @Inject() (connector: BusinessDetailsConnector,
                               repository: LookupRepository,
                               appConfig: AppConfig,
                               timeProvider: TimeProvider,
                               ninoHasher: NinoHasher) {

  private lazy val isHipEnabled: Boolean = FeatureSwitches()(appConfig).isHipEnabled
  private lazy val isMongoLookupEnabled: Boolean = FeatureSwitches()(appConfig).isMongoLookupEnabled

  def getMtdId(nino: String)(implicit
                             correlationId: String,
                             hc: HeaderCarrier,
                             ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    if (isMongoLookupEnabled) {
      lazy val ninoHash: String = ninoHasher.hash(PlainText(nino)).value

      repository.getMtdReference(ninoHash).flatMap {
        case Some(mongoReference) => Future.successful(Right(MtdIdResponse(mongoReference.mtdRef.decryptedValue)))
        case None => getMtdIdFromService(nino)
      }
    } else {
      repository.dropCollection().flatMap(_ => getMtdIdFromService(nino))
    }

  private def getMtdIdFromService(nino: String)(implicit
                                                correlationId: String,
                                                hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    if (isHipEnabled) getMtdIdFromHip(nino) else getMtdIdFromIfs(nino)

  private def getMtdIdFromIfs(nino: String)(implicit
                                            correlationId: String,
                                            hc: HeaderCarrier,
                                            ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    processConnectorResponse[MtdIdIfsReference](connector.getMtdIdFromIfs(nino), nino)

  private def getMtdIdFromHip(nino: String)(implicit
                                            correlationId: String,
                                            hc: HeaderCarrier,
                                            ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    processConnectorResponse[MtdIdHipReference](connector.getMtdIdFromHip(nino), nino)

  private def processConnectorResponse[T <: MtdIdentifier](responseFuture: Future[DownstreamOutcome[T]],
                                                           nino: String)(implicit ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    responseFuture
      .map {
        case Right(response) =>
          if (isMongoLookupEnabled) {
            lazy val ninoHash: String = ninoHasher.hash(PlainText(nino)).value

            lazy val mtdIdCached: MtdIdCached = MtdIdCached(
              ninoHash = ninoHash,
              nino = SensitiveString(nino),
              mtdRef = SensitiveString(response.responseData.mtdbsa),
              lastUpdated = timeProvider.now()
            )

            repository.save(mtdIdCached)
          }

          Right(MtdIdResponse(response.responseData.mtdbsa))
        case Left(ResponseWrapper(_, NotFoundError))  => Left(ForbiddenError)
        case _                                        => Left(InternalError)
      }
}
