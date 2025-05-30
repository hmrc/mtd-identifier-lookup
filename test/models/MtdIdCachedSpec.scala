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

import play.api.libs.json.{JsValue, Json, OFormat}
import support.UnitSpec
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import java.time.Instant

class MtdIdCachedSpec extends UnitSpec {

  private val ninoHash: String = "hashed-nino-value"
  private val nino: SensitiveString = SensitiveString("NS123456A")
  private val mtdRef: SensitiveString = SensitiveString("1234567890")
  private val fixedInstant: Instant  = Instant.parse("2025-01-02T00:00:00.000Z")
  private val mtdIdCached: MtdIdCached = MtdIdCached(
    ninoHash = ninoHash,
    nino = nino,
    mtdRef = mtdRef,
    lastUpdated = fixedInstant
  )

  implicit val crypto: Encrypter with Decrypter = SymmetricCryptoFactory.aesGcmCrypto(
    "fKcVxQ8QFg2U802wmvJlxfWK0dvtaqv7DYiKBH7fzZM="
  )

  implicit val format: OFormat[MtdIdCached] = MtdIdCached.encryptedFormat

  "MtdIdCached" should {
    "serialise and deserialise correctly" in {
      val json: JsValue = Json.toJson(mtdIdCached)
      val model: MtdIdCached = json.as[MtdIdCached]

      model.ninoHash shouldBe mtdIdCached.ninoHash
      model.nino.decryptedValue shouldBe mtdIdCached.nino.decryptedValue
      model.mtdRef.decryptedValue shouldBe mtdIdCached.mtdRef.decryptedValue
      model.lastUpdated shouldBe mtdIdCached.lastUpdated
    }
  }
}
