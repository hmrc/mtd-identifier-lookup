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

package hasher

import mocks.MockAppConfig
import support.UnitSpec
import uk.gov.hmrc.crypto.{PlainText, Scrambled, Sha512Crypto}

class NinoHasherSpec extends UnitSpec with MockAppConfig {

  private val ninoHashKey: String = "KXoSGiJguG+oxzClTjwBAqqjw7YKeUNmqkksJ6Jq37ELfiUmVZ/WlsqXC1QLEd5kMdGQPcDY9SFgTrFoXvDwiQ=="
  private val plainText: PlainText = PlainText("some value")

  private val sha512Crypto: Sha512Crypto = new Sha512Crypto(ninoHashKey)
  private val ninoHasher: NinoHasher = new NinoHasher(mockAppConfig)

  "NinoHasher" when {
    ".hash" should {
      "produce the same hash as Sha512Crypto using the configured ninoHashKey" in {
        MockedAppConfig.ninoHashKey.returns(ninoHashKey)

        val actualResult: Scrambled = ninoHasher.hash(plainText)
        val expectedResult: Scrambled = sha512Crypto.hash(plainText)

        actualResult shouldBe expectedResult
        sha512Crypto.verify(plainText, actualResult) shouldBe true
      }
    }
  }
}
