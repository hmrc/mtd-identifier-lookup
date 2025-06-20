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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration

trait AppConfig {
  def featureSwitches: Configuration

  // IFS Config
  def ifsBaseUrl: String

  def ifsEnv: String

  def ifsToken: String

  def ifsEnvironmentHeaders: Option[Seq[String]]

  lazy val ifsDownstreamConfig: DownstreamConfig = DownstreamConfig(
    baseUrl = ifsBaseUrl,
    env = ifsEnv,
    token = ifsToken,
    environmentHeaders = ifsEnvironmentHeaders
  )

  // HIP Config
  def hipBaseUrl: String

  def hipEnv: String

  def hipClientId: String

  def hipClientSecret: String

  def hipEnvironmentHeaders: Option[Seq[String]]

  lazy val hipDownstreamConfig: BasicAuthDownstreamConfig = BasicAuthDownstreamConfig(
    baseUrl = hipBaseUrl,
    env = hipEnv,
    clientId = hipClientId,
    clientSecret = hipClientSecret,
    environmentHeaders = hipEnvironmentHeaders
  )

  def ttl: Duration

  def ninoHashKey: String
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {

  def ifsBaseUrl: String = config.baseUrl("ifs")

  def ifsEnv: String = config.getString("microservice.services.ifs.env")

  def ifsToken: String = config.getString("microservice.services.ifs.token")

  def ifsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  def hipBaseUrl: String = config.baseUrl("hip")

  def hipEnv: String = config.getString("microservice.services.hip.env")

  def hipClientId: String = config.getString("microservice.services.hip.clientId")

  def hipClientSecret: String = config.getString("microservice.services.hip.clientSecret")

  def hipEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.hip.environmentHeaders")

  def featureSwitches: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def ttl: Duration = config.getDuration("mongodb.ttl")

  def ninoHashKey: String = config.getString("mongodb.ninoHashKey")
}
