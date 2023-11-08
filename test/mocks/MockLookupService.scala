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

import models.domain.MtdIdReference
import models.errors.MtdError
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import services.LookupService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockLookupService extends MockFactory {

  val mockLookupService: LookupService = mock[LookupService]
  implicit val hc: HeaderCarrier       = HeaderCarrier()
  implicit val correlationId: String   = "X-123"
  implicit val ec: ExecutionContext    = scala.concurrent.ExecutionContext.global

  object MockedLookupService {

    def getMtdId(nino: String): CallHandler4[String, String, HeaderCarrier, ExecutionContext, Future[Either[MtdError, MtdIdReference]]] = {
      (mockLookupService
        .getMtdId(_: String)(_: String, _: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *, *)
    }

  }

}
