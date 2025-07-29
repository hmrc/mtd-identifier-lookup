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

import models.MtdIdReference
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class BusinessDetailsConnectorSpec extends ConnectorBaseSpec {

  private val expectedId: String = "an expected Id"
  private val nino: String = "AA123456A"

  "Calling getMtdId with a NINO" should {
    "send a request and return the expected response" in new HipTest {
      val outcome: Right[Nothing, MtdIdReference] = Right(MtdIdReference(expectedId))

      willGet(url"$baseUrl/etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino").returns(Future.successful(outcome))

      await(target.getMtdId(nino)) shouldBe outcome
    }
  }

}
