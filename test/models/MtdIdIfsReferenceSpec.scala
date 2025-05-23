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

class MtdIdIfsReferenceSpec extends UnitSpec {

  private val nino: String = "NS112233A"
  private val mtdId: String = "XAIT0000000000"

  private val desSimJson: JsValue = Json.parse(
    s"""
      |{
      |  "nino": "$nino",
      |  "mtdbsa": "$mtdId"
      |}
    """.stripMargin
  )

  private val ifsJson: JsValue = Json.parse(
    s"""
      |{
      |  "taxPayerDisplayResponse": {
      |    "nino": "$nino",
      |    "mtdId": "$mtdId"
      |  }
      |}
    """.stripMargin
  )

  private val reference: MtdIdIfsReference = MtdIdIfsReference(mtdId)

  "MtdIdIfsReference" when {
    ".reads" should {
      "return the correct MtdIdIfsReference model" when {
        "downstream is IFS" in {
          ifsJson.as[MtdIdIfsReference] shouldBe reference
        }

        "downstream is the des-simulator stub" in {
          desSimJson.as[MtdIdIfsReference] shouldBe reference
        }
      }
    }

    ".mtdbsa" should {
      "return the correct mtdId" in {
        reference.mtdbsa shouldBe mtdId
      }
    }
  }
}
