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

import models.ServiceResponse
import models.errors.AuthError
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockAuthService extends TestSuite with MockFactory {

  val mockAuthService: EnrolmentsAuthService = mock[EnrolmentsAuthService]

  def authoriseUser(): Any = {
    val authResult: ServiceResponse[AuthError, Boolean] = Future.successful(Right(true))

    (mockAuthService
      .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(authResult)
  }

  def unauthorisedUser(): Any = {
    val authResult: ServiceResponse[AuthError, Boolean] = Future.successful(Left(AuthError()))

    (mockAuthService
      .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(authResult)
  }

}
