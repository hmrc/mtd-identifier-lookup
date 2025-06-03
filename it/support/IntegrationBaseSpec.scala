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

package support

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import play.api.{Application, Environment, Mode, inject}
import repositories.LookupRepositoryImpl
import utils.TimeProvider

import java.time.Instant

trait IntegrationBaseSpec
    extends AnyWordSpec
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout
    with WireMockHelper
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  val mockHost: String = WireMockHelper.host
  val mockPort: String = WireMockHelper.wireMockPort.toString

  val fixedInstant: Instant  = Instant.parse("2025-01-02T00:00:00.000Z")

  class FixedTimeProvider extends TimeProvider {
    override def now(): Instant = fixedInstant
  }

  lazy val client: WSClient                 = app.injector.instanceOf[WSClient]
  lazy val repository: LookupRepositoryImpl = app.injector.instanceOf[LookupRepositoryImpl]

  val rootPath: String = s"http://localhost:$port/mtd-identifier-lookup"

  def servicesConfig: Map[String, Any]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .overrides(inject.bind[TimeProvider].toInstance(new FixedTimeProvider))
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.collection.drop().toFuture())
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  /** Creates downstream request body by reading JSON and then writing it back via a model class `A` */
  def downstreamBody[A: Format](json: JsValue): JsValue = Json.toJson(json.as[A])

  /** Creates downstream request body by reading JSON and then writing it back via a model class `A` */
  def downstreamBody[A: Format](json: String): String = downstreamBody(Json.parse(json)).toString()

  def buildRequest(path: String): WSRequest = client.url(s"$rootPath$path").withFollowRedirects(false)

  def document(response: WSResponse): JsValue = Json.parse(response.body)
}
