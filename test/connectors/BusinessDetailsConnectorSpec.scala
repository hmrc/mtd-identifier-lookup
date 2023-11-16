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

import models.{MtdIdDesReference, MtdIdIfsReference}
import play.api.Configuration

import scala.concurrent.Future

class BusinessDetailsConnectorSpec extends ConnectorBaseSpec {

  "Calling getMtdIdFromIfs with a NINO" should {
    "call the business details microservice using the correct URL" in new IfsTest with ConnectorTest {
      val expectedId = "an expected Id"
      val nino       = "AA123456A"
      val reference  = MtdIdIfsReference(expectedId)
      val config     = Configuration("IFSEndpoint.enabled" -> true)
      MockedAppConfig.featureSwitches.returns(config)
      MockedAppConfig.ifsAccept.returns(Some("accept-header"))

      MockHttpClient
        .get(
          s"$baseUrl/registration/business-details/nino/$nino",
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredIfsBusinessDetailsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(Right(reference)))

      await(target.getMtdIdFromIfs(nino))
    }

    "call the business details microservice using the correct URL return an empty result" in new IfsTest with ConnectorTest {
      val nino = "AA123456A"

      val config = Configuration("IFSEndpoint.enabled" -> true)
      MockedAppConfig.featureSwitches.returns(config)
      MockedAppConfig.ifsAccept.returns(Some("accept-header"))

      MockHttpClient
        .get(
          s"$baseUrl/registration/business-details/nino/$nino",
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredIfsBusinessDetailsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(Right(None)))

      (await(target.getMtdIdFromIfs(nino))) shouldBe Right(None)
    }

  }

  "Calling getMtdIdFromDes with a NINO" should {
    "call the business details microservice using the correct URL" in new DesTest with ConnectorTest {
      val expectedId = "an expected Id"
      val nino       = "AA123456A"
      val reference  = MtdIdDesReference(expectedId)
      val config     = Configuration("IFSEndpoint.enabled" -> false)
      MockedAppConfig.featureSwitches.returns(config)
      MockedAppConfig.desOriginator.returns(Some("originator-id"))

      MockHttpClient
        .get(
          s"$baseUrl/registration/business-details/nino/$nino",
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredDesBusinessDetailsHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(Right(reference)))

      await(target.getMtdIdFromDes(nino))
    }
  }

}
