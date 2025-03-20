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

package mocks

import models.MtdIdCached
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import repositories.LookupRepository

import scala.concurrent.Future

trait MockLookupRepository extends MockFactory {

  val mockLookupRepository: LookupRepository = mock[LookupRepository]

  object MockedLookupRepository {

    def save(reference: MtdIdCached): CallHandler[Future[Boolean]] = {
      (mockLookupRepository.save(_:MtdIdCached)).expects(reference)
    }

    def getMtdReference(nino: String): CallHandler[Future[Option[MtdIdCached]]] = {
      (mockLookupRepository.getMtdReference(_: String)).expects(nino)
    }

    def drop(): CallHandler[Future[Long]] = (() => mockLookupRepository.drop()).expects()

  }

}
