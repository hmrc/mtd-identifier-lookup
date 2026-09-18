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

import models.errors.AuthError
import play.api.libs.json.Json
import play.api.mvc.*
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

abstract class AuthorisedController(cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  val authService: EnrolmentsAuthService

  def authorisedAction(predicate: Predicate = EmptyPredicate)(block: Request[AnyContent] => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>

      request.headers.get("Content-Type") match {
        case None                                                                   => logger.info("Content-Type header is missing from the request")
        case Some(contentType) if !contentType.equalsIgnoreCase("application/json") => logger.info(s"Unexpected Content-Type header: $contentType")
        case _                                                                      =>
      }

      authService.authorised(predicate) flatMap {
        case Right(_)                  => block(request)
        case Left(AuthError(false, _)) => Future.successful(Unauthorized(Json.obj()))
        case Left(_)                   => Future.successful(Forbidden(Json.obj()))
      }
  }

}
