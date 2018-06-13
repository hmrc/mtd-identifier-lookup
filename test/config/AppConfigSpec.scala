/*
 * Copyright 2018 HM Revenue & Customs
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

import java.io.File

import org.scalamock.handlers.CallHandler
import play.api.{Configuration, Environment, Mode}
import support.UnitSpec

class AppConfigSpec extends UnitSpec {

  trait Test {
    lazy val dummyFile = new File("./dummy")
    lazy val mockEnvironment: Environment = Environment(dummyFile, mock[ClassLoader], Mode.Prod)
    lazy val mockConfig: Configuration = mock[Configuration]

    def addStringToMockConfig(key: String, value: String): CallHandler[Option[String]] = {
      (mockConfig.getString(_: String, _: Option[Set[String]]))
        .expects(key, *)
        .returns(Some(value))
    }

    def addIntToMockConfig(key: String, value: Int): CallHandler[Option[Int]] = {
      (mockConfig.getInt(_: String))
        .expects(key)
        .returns(Some(value))
    }

    def setupMocks(): Unit = {
      (mockConfig.getString(_: String, _: Option[Set[String]]))
        .expects(*, *)
        .returns(None)
        .anyNumberOfTimes()
    }

    lazy val target: AppConfigImpl = {
      setupMocks()
      new AppConfigImpl(mockEnvironment, mockConfig)
    }
  }

  "calling businessDetailsBaseUrl" should {
    "build up the URL from configuration" in new Test {

      val host = "business-details"
      val port = 9000

      addStringToMockConfig("microservice.services.business-details.host", host)
      addIntToMockConfig("microservice.services.business-details.port", port)

      target.businessDetailsBaseUrl shouldBe s"http://$host:$port"

    }
  }
}
