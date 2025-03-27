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

package repositories

import com.mongodb.BasicDBObject
import config.AppConfig
import models.MtdIdCached
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.ReturnDocument.AFTER
import org.mongodb.scala.model._
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.{Logging, TimeProvider}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.MINUTES
import scala.concurrent.{ExecutionContext, Future}

trait LookupRepository extends Logging {
  def save(reference: MtdIdCached): Future[Boolean]

  def getMtdReference(nino: String): Future[Option[MtdIdCached]]

}

@Singleton
class LookupRepositoryImpl @Inject() (mongo: MongoComponent, timeProvider: TimeProvider)(implicit ec: ExecutionContext,  appConfig: AppConfig)
    extends PlayMongoRepository[MtdIdCached](
      collectionName = "mtdIdLookup",
      mongoComponent = mongo,
      domainFormat = MtdIdCached.format,
      indexes = Seq(
        IndexModel(ascending("nino"), IndexOptions().name("mtd-nino").unique(true).background(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("ttl").expireAfter(appConfig.ttl.toMinutes, MINUTES))
      ),
      replaceIndexes = false
    )
    with LookupRepository {

  def save(reference: MtdIdCached): Future[Boolean] =
    collection
      .replaceOne(
        filter = equal("nino", reference.nino),
        replacement = reference,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
      .recover { case e =>
        logger.warn("Error saving reference to cache", e)
        false
      }

  def removeAll(): Future[DeleteResult] = collection.deleteMany(new BasicDBObject()).toFuture()

  def getMtdReference(nino: String): Future[Option[MtdIdCached]] =
    collection
      .findOneAndUpdate(
        filter = equal("nino", nino),
        update = Updates.set("lastUpdated", timeProvider.now()),
        FindOneAndUpdateOptions()
          .upsert(false)
          .returnDocument(AFTER)
      )
      .toFutureOption()
      .recover { case e =>
        logger.warn("Error retrieving cached reference", e)
        None
      }

}
