/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedControllerSpec extends ControllerBaseSpec with MockEnrolmentsAuthService with LogCapturing {

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

      val result: Future[Result] = testController.authorisedAction() { _ =>
        Future.successful(Ok(Json.obj("message" -> "Success")))
      }(fakeGetRequest)

      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.obj("message" -> "Success")
    }

    "return 401 Unauthorized when not authorised" in {

      (enrollmentAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(Left(AuthError())))

      val result: Future[Result] = testController.authorisedAction() { _ =>
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

      val result: Future[Result] = testController.authorisedAction() { _ =>
        Future.successful(Ok(Json.obj("message" -> "Success")))
      }(fakeGetRequest)

      status(result) shouldBe 403
      contentAsJson(result) shouldBe Json.obj()
    }

    "not log" when {
      "Content-Type is application/json" in {
        val request = fakeGetRequest.withHeaders("Content-Type" -> "application/json")

        (enrollmentAuthService
          .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(Right(true)))

        withCaptureOfLoggingFrom(testController.logger) { events =>
          val result: Future[Result] = testController.authorisedAction() { _ =>
            Future.successful(Ok(Json.obj("message" -> "Success")))
          }(request)

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.obj("message" -> "Success")

          events.map(_.getMessage) shouldBe empty
        }
      }

      "Content-Type is missing" in {
        (enrollmentAuthService
          .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *)
          .returning(Future.successful(Right(true)))

        withCaptureOfLoggingFrom(testController.logger) { events =>
          val result: Future[Result] = testController.authorisedAction() { _ =>
            Future.successful(Ok(Json.obj("message" -> "Success")))
          }(fakeGetRequest)

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.obj("message" -> "Success")

          events.map(_.getMessage) shouldBe empty
        }
      }
    }

    "log when Content-Type is not application/json" in {
      val request = fakeGetRequest.withHeaders("Content-Type" -> "text/plain", "User-Agent" -> "test-user-agent")

      (enrollmentAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(Right(true)))

      withCaptureOfLoggingFrom(testController.logger) { events =>
        val result: Future[Result] = testController.authorisedAction() { _ =>
          Future.successful(Ok(Json.obj("message" -> "Success")))
        }(request)

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.obj("message" -> "Success")

        events.map(_.getMessage) should contain("Unexpected Content-Type header: text/plain for user agent: test-user-agent")
      }
    }
  }

}
