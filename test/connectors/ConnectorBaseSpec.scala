/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.test.ResultExtractors
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

trait ConnectorBaseSpec extends UnitSpec with Status with MimeTypes with HeaderNames with ResultExtractors {

  lazy val baseUrl: String = "http://business-details"

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  val dummyBusinessDetailsHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("mtd-identifier-lookup")
    )

  val requiredBusinessDetailsHeaders: Seq[(String, String)] = Seq(
    "Environment"   -> "business-details-environment",
    "Authorization" -> s"Bearer business-details-token",
    "User-Agent"    -> "mtd-identifier-lookup",
    "Accept"        -> "application/json",
    "Originator-Id" -> "DA_SDI"
  )

  val allowedBusinessDetailsHeaders: Seq[String] = Seq(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

}
