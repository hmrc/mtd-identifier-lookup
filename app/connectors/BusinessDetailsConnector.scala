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

import config.AppConfig
import connectors.httpParsers.MtdIdReadsHttpParser.reader
import javax.inject.{Inject, Singleton}
import models.errors.ExternalServiceError
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessDetailsConnector @Inject() (client: HttpClient, appConfig: AppConfig) {

  private def hcWithDesHeaders(additionalHeaders: Seq[String] = Seq.empty)(implicit hc: HeaderCarrier): HeaderCarrier = {
    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${appConfig.businessDetailsToken}",
          "Environment"   -> appConfig.businessDetailsEnvironment,
          "Accept"        -> "application/json",
          "Originator-Id" -> "DA_SDI"
        ) ++
        // Other headers (i.e Gov-Test-Scenario, Content-Type)
        hc.headers(additionalHeaders ++ appConfig.businessDetailsEnvironmentHeaders.getOrElse(Seq.empty))
    )
  }

  def getMtdId(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ExternalServiceError, String]] = {

    val url = appConfig.businessDetailsBaseUrl + s"/registration/business-details/nino/$nino"

    client.GET(url)(implicitly[HttpReads[Either[ExternalServiceError, String]]], hcWithDesHeaders(), implicitly)
  }

}
