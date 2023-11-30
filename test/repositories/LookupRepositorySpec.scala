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

/*
package repositories

import models.MtdIdCached
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.Logger
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext.Implicits.global

//class LookupRepositoryImplSpec extends AnyWordSpec with Matchers with ScalaFutures with MockFactory {

  val mockMongo: MongoComponent = mock[MongoComponent]

  (mockMongo.client.getDatabase("").getCollection("").insertOne(*)).expects(*).throws(new RuntimeException("Simulated error"))
  class TestLookupRepositoryImpl extends LookupRepositoryImpl(mongo = mockMongo) {

    override val logger: Logger = Logger(this.getClass)
  }

  "LookupRepositoryImpl" should {

    "log a warning message when an error occurs during save" in {
      val repository = new TestLookupRepositoryImpl()
     // val mockLogger = repository.logger
   //   (mockLogger.warn(_: String)).expects(*).once()

      // Perform an action that triggers a warning log
      val reference = MtdIdCached("nino123", "mtdId123")
      (repository.collection.insertOne(_: MtdIdCached)).expects(reference).throws(new RuntimeException("Simulated error"))

      val result: Boolean = repository.save(reference).futureValue

      // Assert the result based on the mocked behavior
      result shouldBe false
    }

    // Add more tests for other log scenarios if needed

  }
 */
//}
