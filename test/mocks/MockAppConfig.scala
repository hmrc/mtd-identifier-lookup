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

package mocks

import config.AppConfig
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.Configuration

trait MockAppConfig extends TestSuite with MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {

    def featureSwitches: CallHandler[Configuration] = (() => mockAppConfig.featureSwitches: Configuration).expects()

    def ninoHashKey: CallHandler[String] = (() => mockAppConfig.ninoHashKey).expects()

    // IFS Config
    def ifsBaseUrl: CallHandler[String] = (() => mockAppConfig.ifsBaseUrl).expects()

    def ifsEnv: CallHandler[String] = (() => mockAppConfig.ifsEnv).expects()

    def ifsToken: CallHandler[String] = (() => mockAppConfig.ifsToken).expects()

    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.ifsEnvironmentHeaders).expects()

    // HIP Config
    def hipBaseUrl: CallHandler[String] = (() => mockAppConfig.hipBaseUrl).expects()

    def hipEnv: CallHandler[String] = (() => mockAppConfig.hipEnv).expects()

    def hipClientId: CallHandler[String] = (() => mockAppConfig.hipClientId).expects()

    def hipClientSecret: CallHandler[String] = (() => mockAppConfig.hipClientSecret).expects()

    def hipEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.hipEnvironmentHeaders).expects()
  }
}
