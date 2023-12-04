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

package controllers

import mocks.MockEnrolmentsAuthService
import models.errors.AuthError
import play.api.libs.json.Json
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Result
import play.api.mvc.Results.Ok

class AuthorisedControllerSpec extends ControllerBaseSpec with MockEnrolmentsAuthService {

  class TestAuthorisedController(enrollmentService: EnrolmentsAuthService) extends AuthorisedController(cc) {
    override val authService: EnrolmentsAuthService = enrollmentService
  }

  val enrollmentAuthService: EnrolmentsAuthService = mockEnrolmentsAuthService

  val testController = new TestAuthorisedController(enrollmentAuthService)

  "AuthorisedController" should {
    "return 200 OK when authorised successfully" in {
      (enrollmentAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(Right(true)))

      val result: Future[Result] = testController.authorisedAction() { request =>
        Future.successful(Ok(Json.obj("message" -> "Success")))
      }(fakeGetRequest)

      // Assertions
      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.obj("message" -> "Success")
    }

    "return 401 Unauthorized when not authorised" in {

      (enrollmentAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(Left(AuthError(false))))

      val result: Future[Result] = testController.authorisedAction() { request =>
        Future.successful(Ok(Json.obj("message" -> "Success")))
      }(fakeGetRequest)

      status(result) shouldBe 401
      contentAsJson(result) shouldBe Json.obj()
    }

    "return 403 Forbidden when an error occurs during authorisation" in {
      (enrollmentAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(Left(AuthError(true))))

      val result: Future[Result] = testController.authorisedAction() { request =>
        Future.successful(Ok(Json.obj("message" -> "Success")))
      }(fakeGetRequest)

      status(result) shouldBe 403
      contentAsJson(result) shouldBe Json.obj()
    }
  }

}
