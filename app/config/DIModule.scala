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

package config

import hasher.NinoHasher
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import repositories.{LookupRepository, LookupRepositoryImpl}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, Hasher, SymmetricCryptoFactory}

class DIModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val cryptoInstance: Encrypter with Decrypter = SymmetricCryptoFactory.aesGcmCryptoFromConfig(
      baseConfigKey = "mongodb.encryption",
      config = configuration.underlying
    )

    Seq(
      bind[AppConfig].to[AppConfigImpl].eagerly(),
      bind[LookupRepository].to[LookupRepositoryImpl],
      bind[Encrypter with Decrypter].toInstance(cryptoInstance),
      bind[Hasher].to[NinoHasher]
    )
  }

}
