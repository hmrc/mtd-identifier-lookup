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

import models.MtdIdIfsReference.convertToMtdIdResponse
import play.api.libs.json.Json
import support.UnitSpec

class MtdIdIfsReferenceSpec extends UnitSpec {

  private val mtdId: String = "XAIT0000000000"

  private val ifsJson = Json.parse(s"""
      |{
      |   "taxPayerDisplayResponse": {
      |       "nino": "NS112233A",
      |       "mtdId": "$mtdId"
      |    }
      |}
     """.stripMargin)

  private val reference: MtdIdIfsReference = MtdIdIfsReference(mtdId)

  "MtdIdIfsReference" should {
    "return the correct MtdId" in {
      reference.mtdbsa shouldBe mtdId
    }
    "convertToMtdIdResponse should return the correct MtdIdResponse" in {
      convertToMtdIdResponse(reference) shouldBe MtdIdResponse(mtdId)
    }
    "reads" should {
      "return the correct model " in {
        ifsJson.as[MtdIdIfsReference] shouldBe reference
      }
    }
  }

}
