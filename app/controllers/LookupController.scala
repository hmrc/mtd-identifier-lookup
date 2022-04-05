/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.domain.Nino
import models.errors.ForbiddenError
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{EnrolmentsAuthService, LookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupController @Inject() (val authService: EnrolmentsAuthService, lookupService: LookupService, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends AuthorisedController(cc) {

  def lookup(nino: String): Action[AnyContent] = authorisedAction() { implicit request =>
    if (Nino.isValid(nino)) {
      lookupService.getMtdId(nino).map {
        case Right(mtdId)         => Ok(Json.obj("mtdbsa" -> mtdId))
        case Left(ForbiddenError) => Forbidden
        case Left(_)              => InternalServerError
      }
    } else {
      Future.successful(BadRequest)
    }

  }

}
