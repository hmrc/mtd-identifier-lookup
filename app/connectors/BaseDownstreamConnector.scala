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

import config.{AppConfig, FeatureSwitches}
import models.connectors.DownstreamOutcome
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector {
  val http: HttpClient
  val appConfig: AppConfig

  // This is to provide an implicit AppConfig in existing connector implementations (which
  // typically declare the abstract `appConfig` field non-implicitly) without having to change them.
  implicit protected lazy val _appConfig: AppConfig = appConfig

  implicit protected lazy val featureSwitches: FeatureSwitches = FeatureSwitches(appConfig.featureSwitches)

  def get[Resp](uri: DownstreamUri[Resp], queryParams: Seq[(String, String)] = Seq.empty)(implicit
                                                                                          ec: ExecutionContext,
                                                                                          hc: HeaderCarrier,
                                                                                          httpReads: HttpReads[DownstreamOutcome[Resp]],
                                                                                          correlationId: String): Future[DownstreamOutcome[Resp]] = {

    val strategy: DownstreamStrategy = uri.strategy

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http.GET(s"${strategy.baseUrl}/${uri.path}", queryParams)

    for {
      headers <- getBackendHeaders(strategy)
      result  <- doGet(headers)
    } yield result
  }

  private def getBackendHeaders(strategy: DownstreamStrategy,
                                additionalHeaders: Seq[(String, String)] = Seq.empty)(implicit
                                                                                      ec: ExecutionContext,
                                                                                      hc: HeaderCarrier,
                                                                                      correlationId: String): Future[HeaderCarrier] = {

    for {
      contractHeaders <- strategy.contractHeaders(correlationId)
    } yield {
      val apiHeaders: Seq[(String, String)] = hc.extraHeaders ++ contractHeaders ++ additionalHeaders

      val passThroughHeaders: Seq[(String, String)] = hc
        .headers(strategy.environmentHeaders)
        .filterNot(hdr => apiHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

      HeaderCarrier(extraHeaders = apiHeaders ++ passThroughHeaders)
    }
  }
}
