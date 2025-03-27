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

import models.MtdIdCached
import support.IntegrationBaseSpec

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

class LookupRepositoryISpec extends IntegrationBaseSpec {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  override def servicesConfig: Map[String, Any] = Map()
  val target: LookupRepositoryImpl              = repository

  val nino: String = "AA123456A"
  val reference: MtdIdCached = MtdIdCached(nino, "id", fixedInstant)

  "calling .save" when {
    "a valid nino and mtdId is passed" should {
      "return true" in {
        val result = target.save(reference)
        await(result) shouldBe true
      }
    }

    "the save fails" should {
      "return false" in {
        val invalidReference = MtdIdCached(None.orNull, "id", fixedInstant)
        val result = target.save(invalidReference)
        await(result) shouldBe false
      }
    }

    "a nino already exists" should {
      "return true" in {
        await(target.save(reference))
        val result = target.save(reference)
        await(result) shouldBe true
      }
    }
  }

  "calling .getMtdReference" when {
    "a valid nino is passed " should {
      "return a mtdId if exists" in {
        await(target.save(reference))
        val result = target.getMtdReference("AA123456A")
        await(result) shouldBe Some(reference)
      }

      "return a none if not exists" in {
        val result = target.getMtdReference("AA123456A")
        await(result) shouldBe None
      }
    }

    "an unexpected database error occurs" should {
      "return None" in {
        val result = target.getMtdReference(None.orNull)

        await(result) shouldBe None
      }
    }
  }

  "calling .getMtdReference multiple times" when {
    "a valid nino is passed" should {
      "return None" in {

        val futures = (1 to 10).map { _ =>
          target.getMtdReference(nino)
        }

        Future.sequence(futures).onComplete {
          case Success(results) =>
            results shouldBe Vector(None, None, None, None, None, None, None, None, None, None)
          case _ =>
            None
        }
      }
    }
  }
}
