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

package models.errors


import play.api.http.Status._

// Notes: Added MtdErrors to be returned in the event of a failure

//This is the only validation - hence the only 400 error
object NinoFormatError extends MtdError(code = "FORMAT_NINO", message = "The provided NINO is invalid", BAD_REQUEST)

object NotFoundError
  extends MtdError(
    code = "MATCHING_RESOURCE_NOT_FOUND",
    message = "Matching resource not found",
    NOT_FOUND
  )

object InternalError
  extends MtdError(
    code = "INTERNAL_SERVER_ERROR",
    message = "An internal server error occurred",
    INTERNAL_SERVER_ERROR
  )

object ForbiddenError
  extends MtdError(code = "CLIENT_OR_AGENT_NOT_AUTHORISED", message = "The client and/or agent is not authorised", FORBIDDEN)

object UnauthorisedError
  extends MtdError(
    code = "CLIENT_OR_AGENT_NOT_AUTHORISED",
    message = "The client and/or agent is not authorised",
    UNAUTHORIZED
  )
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error", SERVICE_UNAVAILABLE)



