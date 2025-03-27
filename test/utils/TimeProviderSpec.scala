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

package utils

import support.UnitSpec

import java.time.Instant

class TimeProviderSpec extends UnitSpec {

  val timeProvider     = new TimeProvider

  "TimeProvider" should {
    "generate an instant" when {
      "now is called" in {
        timeProvider.now().getClass shouldBe Instant.now().getClass
      }
    }
  }
}
