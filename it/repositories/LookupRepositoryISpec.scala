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

package repositories

import models.domain.{MtdIdMongoReference, MtdIdReference}
import support.IntegrationBaseSpec

class LookupRepositoryISpec extends IntegrationBaseSpec {

  val target: LookupRepositoryImpl = repository

  val nino: String              = "AA123456A"
  val mtdId: String             = "id"
  val reference: MtdIdReference = MtdIdReference(mtdId)
  val cached                    = MtdIdMongoReference(nino, mtdId)

  "calling .save" when {
    "a valid nino and mtdId is passed" should {
      "return true" in {
        val result = target.save(cached)
        await(result) shouldBe true
      }
    }
  }

  "calling .getMtdId" when {
    "a valid nino is passed " should {
      "return a mtdId if exists" in {
        target.save(cached)
        val result = target.getMtdReference(nino)
        await(result) shouldBe Some(cached)
      }
    }
  }

  "calling .getMtdId" when {
    "a valid nino is passed " should {
      "return a none if not exists" in {
        val result = target.getMtdReference("not-exist")
        await(result) shouldBe None
      }
    }
  }

}
