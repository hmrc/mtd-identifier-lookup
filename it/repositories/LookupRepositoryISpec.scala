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

package repositories

import models.MtdIdCached
import support.IntegrationBaseSpec
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

class LookupRepositoryISpec extends IntegrationBaseSpec {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  override def servicesConfig: Map[String, Any] = Map()
  val target: LookupRepositoryImpl              = repository

  val ninoHash: String = "hashed-nino-value"
  val nino: SensitiveString = SensitiveString("AA123456A")
  val mtdRef: SensitiveString = SensitiveString("id")
  val mtdIdCached: MtdIdCached = MtdIdCached(
    ninoHash = ninoHash,
    nino = nino,
    mtdRef = mtdRef,
    lastUpdated = fixedInstant
  )

  "calling .save" when {
    "a valid nino and mtdId is passed" should {
      "return true" in {
        val result: Future[Boolean] = target.save(mtdIdCached)

        await(result) shouldBe true
      }
    }

    "the save fails" should {
      "return false" in {
        val invalidReference: MtdIdCached = mtdIdCached.copy(ninoHash = None.orNull)

        val result: Future[Boolean] = target.save(invalidReference)
        await(result) shouldBe false
      }
    }

    "a nino already exists" should {
      "return true" in {
        await(target.save(mtdIdCached))

        val result: Future[Boolean] = target.save(mtdIdCached)
        await(result) shouldBe true
      }
    }
  }

  "calling .getMtdReference" when {
    "a valid ninoHash is passed" should {
      "return a mtdId if exists" in {
        await(target.save(mtdIdCached))

        val result: Future[Option[MtdIdCached]] = target.getMtdReference(ninoHash)
        await(result) shouldBe Some(mtdIdCached)
      }

      "return a none if not exists" in {
        val result: Future[Option[MtdIdCached]] = target.getMtdReference(ninoHash)
        await(result) shouldBe None
      }
    }

    "an unexpected database error occurs" should {
      "return None" in {
        val result: Future[Option[MtdIdCached]] = target.getMtdReference(None.orNull)
        await(result) shouldBe None
      }
    }
  }

  "calling .getMtdReference multiple times" when {
    "a valid ninoHash is passed" should {
      "return None" in {
        val futures: Seq[Future[Option[MtdIdCached]]] = (1 to 10).map { _ =>
          target.getMtdReference(ninoHash)
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

  "calling .dropCollection" should {
    "drop the collection and return count 0" in {
      await(target.save(mtdIdCached))

      val count: Long = await(target.dropCollection())

      count shouldBe 0L
      await(target.getMtdReference(ninoHash)) shouldBe None
    }
  }
}
