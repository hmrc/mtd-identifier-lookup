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

import javax.inject.{Inject, Singleton}
import models.domain.Nino
import models.errors.{ForbiddenError, InternalError, NinoFormatError, NotEnrolledError}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{EnrolmentsAuthService, LookupService}
import utils.IdGenerator

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LookupController @Inject() (val authService: EnrolmentsAuthService, lookupService: LookupService, cc: ControllerComponents)(implicit
    ec: ExecutionContext,
    idGenerator: IdGenerator)
    extends AuthorisedController(cc) {
  implicit val correlationId: String = idGenerator.generateCorrelationId

  def lookup(nino: String, notEnrolledFlag: Option[Boolean]): Action[AnyContent] = authorisedAction() { implicit request =>
    if (Nino.isValid(nino)) {
      val notEnrolledFlagValue: Boolean = if (notEnrolledFlag.isEmpty) false else notEnrolledFlag.get
      val notEnroledHeader: Boolean     = request.headers.get("Gov-Test-Scenario").contains("NOT_ENROLLED")
      val result = lookupService
        .getMtdId(nino, notEnrolledFlag = notEnrolledFlagValue, notEnroledHeader = notEnroledHeader)
      result.map {
        case Right(reference)       => Ok(Json.toJson(reference))
        case Left(ForbiddenError)   => Forbidden(ForbiddenError.asJson)
        case Left(NotEnrolledError) => UnprocessableEntity(NotEnrolledError.asJson)
        case Left(_)                => InternalServerError(InternalError.asJson)
      }
    } else {
      Future.successful(BadRequest(NinoFormatError.asJson))
    }
  }

}
