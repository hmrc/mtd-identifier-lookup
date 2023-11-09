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

import mocks.{MockAppConfig, MockHttpClient}
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ConnectorBaseSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl: String = "http://business-details"

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
   // "Accept"            -> "1.0",
    "CorrelationId"     -> correlationId
  )

  val dummyBusinessDetailsHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-identifier-lookup")
    )

  val requiredBusinessDetailsHeaders: Seq[(String, String)] = Seq(
 "Environment" -> "business-details-environment",
    "Authorization"->"Bearer business-details-token",
    "User-Agent"-> "mtd-identifier-lookup",
    "Originator-Id"->"DA_SDI",
    "Gov-Test-Scenario"->"DEFAULT",
    "Accept"->"application/json",
    //"CorrelationId"->"X-123"
     )
  // ("CorrelationId", "X-123"), ("Accept", "1.0"), ("Gov-Test-Scenario", "DEFAULT")
  val allowedBusinessDetailsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val correlationId        = "X-123"
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {

    // protected val baseUrl: String = "http://test-BaseUrl"
    val target: BusinessDetailsConnector = {
      new BusinessDetailsConnector(mockHttpClient, mockAppConfig)
    }

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]

    protected def excludedHeaders: Seq[(String, String)] = Seq("AnotherHeader" -> "HeaderValue")

    protected def willGet[T](url: String, parameters: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      MockHttpClient
        .get(
          url = url,
          parameters = parameters,
          config = dummyBusinessDetailsHeaderCarrierConfig,
          requiredHeaders = requiredBusinessDetailsHeaders,
          excludedHeaders = excludedHeaders
        )
    }

  }

  protected trait IfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredBusinessDetailsHeaders

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnv returns "ifs-environment"
    MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedBusinessDetailsHeaders)
  }

}
