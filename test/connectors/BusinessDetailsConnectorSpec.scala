/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import mocks.{MockAppConfig, MockHttpClient}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BusinessDetailsConnectorSpec extends ConnectorBaseSpec {

  trait Test extends MockHttpClient with MockAppConfig {
    lazy val target: BusinessDetailsConnector = {
      new BusinessDetailsConnector(mockHttpClient, mockAppConfig)
    }
  }

  "Calling .getMtdId with a NINO" should {
    "call the business details microservice using the correct URL" in new Test {
      val expectedId = "an expected Id"
      MockedAppConfig.businessDetailsBaseUrl().returns("http://business-details")
      MockedAppConfig.businessDetailsToken().returns("something")
      MockedAppConfig.businessDetailsEnvironment().returns("something")
      mockGet("http://business-details/registration/business-details/nino/AA123456A")
        .returns(Future.successful(Right(expectedId)))

      await(target.getMtdId("AA123456A"))
    }
  }
}
