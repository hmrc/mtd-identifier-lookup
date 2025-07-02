/*
 * Copyright 2025 HM Revenue & Customs
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

package endpoints

import models.errors.ForbiddenError
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{FORBIDDEN, NOT_FOUND}

class LookupControllerIfsISpec extends BaseControllerISpec {

  val downstreamUrl: String = s"/registration/business-details/nino/$nino"

  val downstreamQueryParam: Map[String, String] = Map.empty

  val downstreamResponseJson: JsValue = Json.parse(
    s"""
      |{
      |  "taxPayerDisplayResponse": {
      |    "nino": "$nino",
      |    "mtdId": "$mtdId"
      |  }
      |}
    """.stripMargin
  )

  val errorBody: String =
    """
      |{
      |  "code": "NOT_FOUND",
      |  "reason": "error message from downstream"
      |}
    """.stripMargin

  responseSuccessful()
  notLoggedIn()
  unauthorised()
  responseFailures(NOT_FOUND, "NOT_FOUND", errorBody, FORBIDDEN, ForbiddenError)
}
