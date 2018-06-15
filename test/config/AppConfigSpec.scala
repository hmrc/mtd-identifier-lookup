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

    val env = "STUB"
    val host = "business-details"
    val port = 9000
    val token = "Bearer STUB"

    private def addStringToMockConfig(key: String, value: String): CallHandler[Option[String]] = {
      (mockConfig.getString(_: String, _: Option[Set[String]]))
        .stubs(key, *)
        .returns(Some(value))
    }

    private def addIntToMockConfig(key: String, value: Int): CallHandler[Option[Int]] = {
      (mockConfig.getInt(_: String))
        .stubs(key)
        .returns(Some(value))
    }

    def configStrings: Map[String, String] = Map[String, String](
      "microservice.services.business-details.host" -> host,
      "microservice.services.business-details.token" -> token,
      "microservice.services.business-details.env" -> env
    )

    def configInts: Map[String, Int] = Map[String, Int](
      "microservice.services.business-details.port" -> port
    )

    def setupMocks(): Unit = {
      configStrings.foreach {
        case (k, v) => addStringToMockConfig(k, v)
      }
      configInts.foreach {
        case (k, v) => addIntToMockConfig(k, v)
      }

      (mockConfig.getString(_: String, _: Option[Set[String]]))
        .stubs(*, *)
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
      target.businessDetailsBaseUrl shouldBe s"http://$host:$port"
    }
  }

  "calling desEnv" when {

    "env is added to the configuration" should {
      "return the business details env" in new Test {
        target.businessDetailsEnvironment shouldBe env
      }
    }

    "no env is added to the configuration" should {
      "return the run time exception" in new Test {
        override def configStrings: Map[String, String] = {
          super.configStrings.filterNot {
            case (k, _) => k == "microservice.services.business-details.env"
          }
        }
        intercept[RuntimeException] {
          target.businessDetailsEnvironment
        }
      }
    }
  }

  "calling desToken" when {

    "token is added to the configuration" should {
      "return the business details token" in new Test {
        target.businessDetailsToken shouldBe token
      }
    }

    "no desToken is added to the configuration" should {
      "return the run time exception" in new Test {
        override def configStrings: Map[String, String] = {
          super.configStrings.filterNot {
            case (k, _) => k == "microservice.services.business-details.token"
          }
        }
        intercept[RuntimeException] {
          target.businessDetailsToken
        }
      }
    }
  }
}
