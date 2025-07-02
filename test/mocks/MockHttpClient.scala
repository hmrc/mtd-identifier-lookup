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

package mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import utils.UrlUtils

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends TestSuite with MockFactory {

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  object MockHttpClient extends Matchers {

    def get[T](url: URL,
               config: HeaderCarrier.Config,
               parameters: Seq[(String, String)] = Nil,
               requiredHeaders: Seq[(String, String)] = Nil,
               excludedHeaders: Seq[(String, String)] = Nil): CallHandler[Future[T]] = {
      (mockHttpClient
        .get(_: URL)(_: HeaderCarrier))
        .expects(assertArgs { (actualUrl: URL, hc: HeaderCarrier) =>
        {
          val expectedURL = UrlUtils.appendQueryParams(url.toString, parameters)
          actualUrl.toString shouldBe expectedURL

          val headersForUrl = hc.headersForUrl(config)(actualUrl.toString)
          assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
        }
        })
        .returns(mockRequestBuilder)
      (mockRequestBuilder.execute(_: HttpReads[T], _: ExecutionContext)).expects(*, *)
    }

    private def assertHeaders[T, I](actualHeaders: Seq[(String, String)],
                                    requiredHeaders: Seq[(String, String)],
                                    excludedHeaders: Seq[(String, String)]) = {

      actualHeaders should contain allElementsOf requiredHeaders
      actualHeaders should contain noElementsOf excludedHeaders
    }

  }

}
