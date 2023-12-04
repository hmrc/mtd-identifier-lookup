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

package models

import play.api.libs.json.Json
import support.UnitSpec

class MtdIdDesReferenceSpec extends UnitSpec {

  val mtdbsa: String = "XAIT0000000000"

  private val desJson = Json.parse(
    s"""
      |{
      |  "nino": "NS112233A",
      |  "mtdbsa": "$mtdbsa"
      |}
     """.stripMargin
  )

  private val reference: MtdIdDesReference = MtdIdDesReference(mtdbsa)

  "MtdIdDesReference" should {
    "return the correct MtdId" in {
      reference.mtdbsa shouldBe mtdbsa
    }

    "reads" should {
      "return the correct model " in {
        desJson.as[MtdIdDesReference] shouldBe reference
      }
    }
  }

}
