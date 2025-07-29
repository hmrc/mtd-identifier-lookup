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

package models

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class MtdIdReferenceSpec extends UnitSpec {

  private val mtdId: String = "XAIT0000000000"

  private val downstreamJson: JsValue = Json.parse(
    s"""
      |{
      |  "success": {
      |    "taxPayerDisplayResponse": {
      |      "mtdId": "$mtdId"
      |    }
      |  }
      |}
    """.stripMargin
  )

  private val reference: MtdIdReference = MtdIdReference(mtdId)

  "MtdIdReference" when {
    ".reads" should {
      "return the correct MtdIdReference model" in {
        downstreamJson.as[MtdIdReference] shouldBe reference
      }
    }

    ".mtdbsa" should {
      "return the correct mtdId" in {
        reference.mtdbsa shouldBe mtdId
      }
    }
  }
}
