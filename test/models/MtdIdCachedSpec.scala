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

import java.time.Instant

class MtdIdCachedSpec extends UnitSpec {

  private val nino: String = "NS123456A"
  private val mtdRef: String = "1234567890"
  private val fixedInstant: Instant  = Instant.parse("2025-01-02T00:00:00.000Z")
  private val reference: MtdIdCached = MtdIdCached(nino, mtdRef, fixedInstant)
  private val cachedJson = Json.parse(s"""
      |{
      |   "nino":"$nino",
      |   "mtdRef":"$mtdRef",
      |   "lastUpdated": { "$$date": { "$$numberLong": "${fixedInstant.toEpochMilli}" } }
      |}
    """.stripMargin)

  "MtdIdCached" should {
    "return the correct MtdId" in {
      reference.mtdRef shouldBe mtdRef
      reference.nino shouldBe nino
      reference.lastUpdated shouldBe fixedInstant
    }
    "reads" should {
        "return the correct model in " in {
           cachedJson.as[MtdIdCached] shouldBe reference
        }
    }
  }

}
