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

package controllers

import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, ResultExtractors}
import support.UnitSpec
import utils.IdGenerator

import scala.concurrent.ExecutionContext

trait ControllerBaseSpec extends UnitSpec with Status with MimeTypes with HeaderNames with ResultExtractors {

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val idGenerator:IdGenerator = new IdGenerator()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global



  lazy val cc: ControllerComponents = stubControllerComponents()

  lazy val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  def fakePostRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)
}