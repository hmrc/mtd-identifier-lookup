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
import connectors.BusinessDetailsConnector

import scala.util.control.NonFatal
import models.domain.{MtdIdMongoReference, MtdIdReference}
import models.errors.{ForbiddenError, InternalError, MtdError, NotFoundError}
import models.outcomes.ResponseWrapper
import repositories.LookupRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupService @Inject() (connector: BusinessDetailsConnector, repository: LookupRepository) extends Logging {

  def getMtdId(nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdReference]] = {
    val result = repository.getMtdReference(nino)
    result.flatMap {
      case Some(mongoReference) => Future.successful(Right(MtdIdReference(mongoReference.mtdRef)))
      case None =>
        getMtdIdFromService(nino)
    }
  }

  private def getMtdIdFromService(
      nino: String)(implicit correlationId: String, hc: HeaderCarrier, ec: ExecutionContext): Future[Either[MtdError, MtdIdReference]] = {
    val result = for {
      mtdResponse <- connector.getMtdId(nino).map {
        case Right(response) => { // Note: If we use a stateless stub that returns the nino submitted, then we don't have to do this
          repository.save(MtdIdMongoReference(nino, response.responseData.mtdbsa))
          Right(MtdIdReference(response.responseData.mtdbsa))
        }
        case Left(ResponseWrapper(_, NotFoundError))  => Left(ForbiddenError)
        case Left(ResponseWrapper(_, ForbiddenError)) => Left(InternalError)
        case _                                        => Left(InternalError)
      }

    } yield mtdResponse

    result.recover { case NonFatal(e) =>
      logger.error(s"Error getting MTD ID for nino $nino", e)
      Left(InternalError)
    }
  }

}
