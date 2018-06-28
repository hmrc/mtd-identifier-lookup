/*
 * Copyright 2018 HM Revenue & Customs
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

import com.mongodb.BasicDBObject
import com.mongodb.casbah.MongoClient
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.DefaultDB
import support.UnitSpec
import uk.gov.hmrc.mongo.MongoConnector

trait MongoEmbeddedDatabase extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  import MongoEmbeddedDatabase._

  implicit val mongo: () => DefaultDB = MongoConnector(mongoUri).db

  lazy private val mongoClient =
    MongoClient("localhost", port)("mtd-identifier-lookup")

  override def beforeEach(): Unit = mongoClient.getCollection("mtdIdLookup").remove(new BasicDBObject())

  override def beforeAll(): Unit = {
    buildExecutable.start()
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    buildExecutable.stop()
    super.afterAll()
  }

}

object MongoEmbeddedDatabase {
  private val port = 12345
  private val mongoUri = sys.env.getOrElse("MONGO_TEST_URI", s"mongodb://localhost:$port/mtd-identifier-lookup")

  private val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build()

  private val buildExecutable: MongodExecutable = {
    MongodStarter.getInstance(runtimeConfig).prepare(
      new MongodConfigBuilder()
        .version(Version.valueOf("V3_2_1"))
        .net(new Net(port, Network.localhostIsIPv6()))
        .build()
    )
  }
}
