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

package connectors

//import mocks.{MockAppConfig, MockHttpClient}
import models.domain.MtdIdReference
import play.api.Configuration

import scala.concurrent.Future

class BusinessDetailsConnectorSpec extends ConnectorBaseSpec {

  // class Test(businessDetailsEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

  // }

  "Calling .getMtdId with a NINO" should {
    "call the business details microservice using the correct URL" in new IfsTest
      with ConnectorTest {
      val expectedId = "an expected Id"
      val nino       = "AA123456A"
      val reference  = MtdIdReference(expectedId)
      val config     = Configuration("IFSEndpoint.enabled" -> true)
      MockedAppConfig.featureSwitches.returns(config)
      MockedAppConfig.ifsBaseUrl.returns(baseUrl)
      MockedAppConfig.ifsToken.returns("business-details-token")
      MockedAppConfig.ifsEnv.returns("business-details-environment")
      MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedBusinessDetailsHeaders)

      MockHttpClient
        .get(
          s"$baseUrl/registration/business-details/nino/$nino",
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredBusinessDetailsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(Right(reference)))

      await(target.getMtdId(nino))
    }
  }

}
