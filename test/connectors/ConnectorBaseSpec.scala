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

import com.google.common.base.Charsets
import mocks.{MockAppConfig, MockHttpClient}
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.net.URL
import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

trait ConnectorBaseSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl: String = "http://business-details"

  implicit val hc: HeaderCarrier     = HeaderCarrier()
  implicit val correlationId: String = "X-123"
  implicit val ec: ExecutionContext  = scala.concurrent.ExecutionContext.global

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "CorrelationId"     -> correlationId
  )

  val dummyBusinessDetailsHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-identifier-lookup")
    )

  val allowedBusinessDetailsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {

    val target: BusinessDetailsConnector = new BusinessDetailsConnector(mockHttpClient, mockAppConfig)

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]

    protected def excludedHeaders: Seq[(String, String)] = Seq("AnotherHeader" -> "HeaderValue")

    protected def willGet[T](url: URL, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockHttpClient
        .get(
          url = url,
          parameters = parameters,
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        )
    }
  }

  protected trait HipTest extends ConnectorTest {
    private val clientId: String = "clientId"
    private val clientSecret: String = "clientSecret"

    private val token: String = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(Charsets.UTF_8))

    MockedAppConfig.hipBaseUrl returns baseUrl
    MockedAppConfig.hipEnv returns "hip-environment"
    MockedAppConfig.hipClientId returns clientId
    MockedAppConfig.hipClientSecret returns clientSecret
    MockedAppConfig.hipEnvironmentHeaders returns Some(allowedBusinessDetailsHeaders)

    protected lazy val requiredHeaders: Seq[(String, String)] = Seq(
      "Authorization"     -> s"Basic $token",
      "Environment"       -> "hip-environment",
      "User-Agent"        -> "mtd-identifier-lookup",
      "Gov-Test-Scenario" -> "DEFAULT"
    )
  }
}
