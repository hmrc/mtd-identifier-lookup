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

package mocks

import models.MtdIdReference
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.LookupRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockLookupRepository extends MockFactory{

  val mockLookupRepository = mock[LookupRepository]

  object MockedLookupRepository {

    def save(nino: String, mtdId: String): CallHandler[Future[Boolean]] = {
      (mockLookupRepository.save(_: String, _:String)(_: HeaderCarrier, _: ExecutionContext)).expects(nino, mtdId, *, *)
    }

    def getMtdReference(nino: String): CallHandler[Future[Option[MtdIdReference]]] = {
      (mockLookupRepository.getMtdReference(_: String)(_: HeaderCarrier, _: ExecutionContext)).expects(nino, *, *)
    }
  }
}
