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

import config.AppConfig
import uk.gov.hmrc.crypto.{Hasher, PlainText, Scrambled, Sha512Crypto}

import javax.inject.{Inject, Singleton}

@Singleton
class NinoHasher @Inject()(appConfig: AppConfig) extends Hasher {
  private lazy val ninoHasher: Sha512Crypto = new Sha512Crypto(appConfig.ninoHashKey)

  override def hash(plain: PlainText): Scrambled = ninoHasher.hash(plain)
}
