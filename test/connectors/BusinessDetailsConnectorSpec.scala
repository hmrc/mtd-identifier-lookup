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

package connectors

import models.{MtdIdHipReference, MtdIdIfsReference}
import play.api.Configuration
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class BusinessDetailsConnectorSpec extends ConnectorBaseSpec {

  private val expectedId: String = "an expected Id"
  private val nino: String = "AA123456A"
  private def config(isHipEnabled: Boolean): Configuration = Configuration(
    "ifs_hip_migration_1171.enabled" -> isHipEnabled
  )

  "Calling getMtdIdFromIfs with a NINO" should {
    "send a request and return the expected response when downstream is IFS" in new IfsTest {
      val outcome: Right[Nothing, MtdIdIfsReference] = Right(MtdIdIfsReference(expectedId))
      MockedAppConfig.featureSwitches.returns(config(false))

      willGet(url"$baseUrl/registration/business-details/nino/$nino").returns(Future.successful(outcome))

      await(target.getMtdIdFromIfs(nino)) shouldBe outcome
    }
  }

  "Calling getMtdIdFromHip with a NINO" should {
    "send a request and return the expected response when downstream is HIP" in new HipTest {
      val outcome: Right[Nothing, MtdIdHipReference] = Right(MtdIdHipReference(expectedId))
      MockedAppConfig.featureSwitches.returns(config(true))

      willGet(url"$baseUrl/etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino").returns(Future.successful(outcome))

      await(target.getMtdIdFromHip(nino)) shouldBe outcome
    }
  }

}
