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

package models.domain

import play.api.libs.json.Json
import support.UnitSpec

class MtdIdReferenceSpec extends UnitSpec {
  private val reference = MtdIdReference("XAIT0000000000")

  private val desJson = Json.parse(
    """
      |{
      |  "nino": "NS112233A",
      |  "mtdRef": "XAIT0000000000"
      |}
    """.stripMargin
  )

  private val ifsJson = Json.parse(
    """
      |{
      |   "taxPayerDisplayResponse": {
      |       "nino": "NS112233A",
      |       "mtdId": "XAIT0000000000"
      |    }
      |}
    """.stripMargin
  )

  private val modelJson = Json.parse(
    """
    |{
    |   "mtdbsa":"XAIT0000000000"
    |}
    """.stripMargin
  )

  "reads" when {
    "passed a valid DES model " should {
      "return a valid MtdIdReference model" in {
        desJson.as[MtdIdReference] shouldBe reference
      }
    }
    "passed a valid IFS model" should {
      "return a valid MtdIdReference model" in {
        ifsJson.as[MtdIdReference] shouldBe reference
      }
    }
  }

  "writes" when {
    "passed a valid MtdIdReference model" should {
      "return valid reference json" in {
        Json.toJson(reference) shouldBe modelJson
      }
    }
  }

}
