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

import config.AppConfig
import connectors.DownstreamUri.{HipUri, IfsUri}
import connectors.httpParsers.StandardDownstreamHttpParser._
import models.{MtdIdHipReference, MtdIdIfsReference}
import models.connectors.DownstreamOutcome
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessDetailsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def getMtdIdFromIfs(nino: String)(implicit
                                    hc: HeaderCarrier,
                                    ec: ExecutionContext,
                                    correlationId: String): Future[DownstreamOutcome[MtdIdIfsReference]] =
    get(IfsUri[MtdIdIfsReference](s"registration/business-details/nino/$nino"))

  def getMtdIdFromHip(nino: String)(implicit
                                    hc: HeaderCarrier,
                                    ec: ExecutionContext,
                                    correlationId: String): Future[DownstreamOutcome[MtdIdHipReference]] =
    get(HipUri[MtdIdHipReference](s"etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino"))
}
