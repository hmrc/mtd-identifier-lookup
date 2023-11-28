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
import connectors.DownstreamUri.{DesUri, IfsUri}
import connectors.httpParsers.StandardDownstreamHttpParser._
import models.{MtdIdDesReference, MtdIdIfsReference}
import models.connectors.DownstreamOutcome
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessDetailsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def getMtdIdFromDes(
      nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[MtdIdDesReference]] = {
    val url = DesUri[MtdIdDesReference](s"registration/business-details/nino/$nino")
    get(url)
  }

  def getMtdIdFromIfs(
      nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[MtdIdIfsReference]] = {
    val url = IfsUri[MtdIdIfsReference](s"registration/business-details/nino/$nino")
    get(url)
  }

}
