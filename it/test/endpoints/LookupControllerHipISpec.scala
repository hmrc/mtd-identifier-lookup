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

import models.errors.{ForbiddenError, InternalError}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{FORBIDDEN, INTERNAL_SERVER_ERROR, UNPROCESSABLE_ENTITY}

class LookupControllerHipISpec extends BaseControllerISpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1171.enabled" -> true) ++ super.servicesConfig

  val downstreamUrl: String = "/etmp/RESTAdapter/itsa/taxpayer/business-details"

  val downstreamQueryParam: Map[String, String] = Map("nino" -> nino)

  val downstreamResponseJson: JsValue = Json.parse(
    s"""
      |{
      |  "success": {
      |    "processingDate": "2023-07-05T09:16:58Z",
      |    "taxPayerDisplayResponse": {
      |      "safeId": "XAIS123456789012",
      |      "nino": "$nino",
      |      "mtdId": "$mtdId",
      |      "propertyIncomeFlag": false,
      |      "businessData": [
      |        {
      |          "incomeSourceId": "XAIS12345678901",
      |          "accPeriodSDate": "2001-01-01",
      |          "accPeriodEDate": "2001-01-01",
      |          "cashOrAccrualsFlag": false
      |        }
      |      ]
      |    }
      |  }
      |}
    """.stripMargin
  )

  def errorBody(code: String): String =
    s"""
      |{
      |  "errors": {
      |    "processingDate": "2024-07-15T09:45:17Z",
      |    "code": "$code",
      |    "text": "some text"
      |  }
      |}
    """.stripMargin

  responseSuccessful()
  notLoggedIn()
  unauthorised()
  Seq(
    (UNPROCESSABLE_ENTITY, "001", INTERNAL_SERVER_ERROR, InternalError),
    (UNPROCESSABLE_ENTITY, "006", FORBIDDEN, ForbiddenError),
    (UNPROCESSABLE_ENTITY, "007", INTERNAL_SERVER_ERROR, InternalError),
    (UNPROCESSABLE_ENTITY, "008", FORBIDDEN, ForbiddenError)
  ).foreach { case (downstreamStatus, downstreamCode, expectedStatus, expectedBody) =>
    responseFailures(downstreamStatus, downstreamCode, errorBody(downstreamCode), expectedStatus, expectedBody)
  }
}
