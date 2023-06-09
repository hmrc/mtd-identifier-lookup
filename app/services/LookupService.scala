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
import models.errors.{ExternalServiceError, ForbiddenError, InternalServerError, NotFoundError}
import repositories.LookupRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupService @Inject() (connector: BusinessDetailsConnector, repository: LookupRepository) {

  def getMtdId(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ExternalServiceError, String]] = {
    repository.getMtdReference(nino).flatMap {
      case Some(mtdIdReference) => Future.successful(Right(mtdIdReference.mtdRef))
      case None                 => getMtdIdFromBusinessDetailsApi(nino)
    }
  }

  private[services] def getMtdIdFromBusinessDetailsApi(
      nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ExternalServiceError, String]] = {
    connector.getMtdId(nino).map {
      case success @ Right(mtdId) =>
        repository.save(nino, mtdId)
        success
      case Left(NotFoundError) => Left(ForbiddenError)
      case Left(_)             => Left(InternalServerError)
    }
  }

}
