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

import connectors.DownstreamUri.DesUri
import config.{AppConfig, FeatureSwitches}
import models.connectors.DownstreamOutcome
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector {
  val http: HttpClient
  val appConfig: AppConfig

  protected val logger: Logger = Logger(this.getClass)

  implicit protected lazy val featureSwitches: FeatureSwitches = FeatureSwitches(appConfig.featureSwitches)

  def get[Resp](uri: DownstreamUri[Resp])(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http.GET(getBackendUri(uri))

    }
    doGet(getBackendHeaders(uri, hc, correlationId))
  }

  private def getBackendUri[Resp](uri: DownstreamUri[Resp]): String =
    s"${configFor(uri).baseUrl}/${uri.value}"

  private def getBackendHeaders[Resp](
      uri: DownstreamUri[Resp],
      hc: HeaderCarrier,
      correlationId: String,
      additionalHeaders: (String, String)*
  ): HeaderCarrier = {
    val downstreamConfig = configFor(uri)

    val passThroughHeaders = hc
      .headers(downstreamConfig.environmentHeaders.getOrElse(Seq.empty))
      .filterNot(hdr => additionalHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

    // required for stub
    val downstreamHeader: Option[(String, String)] =
      (downstreamConfig.accept, downstreamConfig.originator) match {
        case (_, Some(header)) => Some("Originator-Id" -> header)
        case (Some(header), _) => Some("Accept" -> header)

        case _ => None
      }

    val headersToAdd: Seq[(String, String)] = Seq(
      "Authorization" -> s"Bearer ${downstreamConfig.token}",
      "Environment"   -> downstreamConfig.env,
      "CorrelationId" -> correlationId
    ) ++ additionalHeaders ++ passThroughHeaders ++ downstreamHeader.toSeq

    HeaderCarrier(extraHeaders = hc.extraHeaders ++ headersToAdd)
  }

  private def configFor[Resp](uri: DownstreamUri[Resp]) =
    uri match {
      case DesUri(_) => appConfig.desDownstreamConfig
      case _         => appConfig.ifsDownstreamConfig
    }

}
