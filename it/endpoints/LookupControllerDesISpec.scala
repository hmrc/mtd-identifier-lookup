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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package endpoints

import models.MtdIdResponse
import play.api.libs.json.Json

class LookupControllerDesISpec extends BaseControllerISpec {

  override def servicesConfig: Map[String, Any] = super.servicesConfig ++ Map(
    "feature-switch.ifs.enabled" -> false
  )

  val nino      = "AA123456A"
  val mtdId     = "1234567890"
  val reference = MtdIdResponse(mtdId)

  val connectorResponseJson = Json.parse(
    s"""
      |{
      |    "mtdbsa": "$mtdId"
      |}
  """.stripMargin
  )

  val responseJson = Json.parse(
    s"""
      |{
      |    "mtdbsa": "$mtdId"
      |}
   """.stripMargin
  )

  responseSuccessful()
  notLoggedIn()
  unauthorised()
  nonMtdNino()

}
