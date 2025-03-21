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

import com.google.inject.{Inject, Singleton}
import config.{AppConfig, FeatureSwitches}
import connectors.BusinessDetailsConnector
import models.connectors.DownstreamOutcome
import models.errors.{ForbiddenError, InternalError, MtdError, NotFoundError}
import models.outcomes.ResponseWrapper
import models._
import repositories.LookupRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Logging, TimeProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupService @Inject() (connector: BusinessDetailsConnector,
                               repository: LookupRepository,
                               appConfig: AppConfig,
                               timeProvider: TimeProvider
                              ) extends Logging {

  private lazy val isIfsEnabled: Boolean = FeatureSwitches()(appConfig).isIfsEnabled()

  def getMtdId(nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] = {
    val result = repository.getMtdReference(nino)
    result.flatMap {
      case Some(mongoReference) => Future.successful(Right(MtdIdResponse(mongoReference.mtdRef)))
      case None =>
        getMtdIdFromService(nino)
    }
  }

  private def getMtdIdFromService(
      nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] = {

    if (isIfsEnabled) getMtdIdFromIfs(nino)  else getMtdIdFromDes(nino)

  }

  private def getMtdIdFromDes(
      nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    processConnectorResponse[MtdIdDesReference](connector.getMtdIdFromDes(nino), nino)

  private def getMtdIdFromIfs(
      nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] =
    processConnectorResponse[MtdIdIfsReference](connector.getMtdIdFromIfs(nino), nino)

  private def processConnectorResponse[T <: MtdIdentifier](responseFuture: Future[DownstreamOutcome[T]], nino: String)(implicit
      ec: ExecutionContext): Future[Either[MtdError, MtdIdResponse]] = {

    responseFuture
      .map {
        case Right(response) =>
          repository.save(MtdIdCached(nino, response.responseData.mtdbsa, timeProvider.now()))
          Right(MtdIdResponse(response.responseData.mtdbsa))

        case Left(ResponseWrapper(_, NotFoundError))  => Left(ForbiddenError)
        case Left(ResponseWrapper(_, ForbiddenError)) => Left(InternalError)
        case _                                        => Left(InternalError)
      }
  }

}
