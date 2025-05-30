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

import com.mongodb.BasicDBObject
import config.AppConfig
import models.MtdIdCached
import org.mongodb.scala.DuplicateKeyException
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.ReturnDocument.AFTER
import org.mongodb.scala.model._
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.{Logging, TimeProvider}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.MINUTES
import scala.concurrent.{ExecutionContext, Future}

trait LookupRepository extends Logging {
  def save(reference: MtdIdCached): Future[Boolean]

  def getMtdReference(ninoHash: String): Future[Option[MtdIdCached]]

  def dropCollection(): Future[Long]
}

@Singleton
class LookupRepositoryImpl @Inject() (mongo: MongoComponent, timeProvider: TimeProvider)(implicit
                                                                                         ec: ExecutionContext,
                                                                                         crypto: Encrypter with Decrypter,
                                                                                         appConfig: AppConfig)
    extends PlayMongoRepository[MtdIdCached](
      collectionName = "mtdIdLookup",
      mongoComponent = mongo,
      domainFormat = MtdIdCached.encryptedFormat,
      indexes = Seq(
        IndexModel(ascending("ninoHash"), IndexOptions().name("ninoHashIndex").unique(true).background(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("ttl").expireAfter(appConfig.ttl.toMinutes, MINUTES))
      ),
      replaceIndexes = false
    )
    with LookupRepository {

  def save(reference: MtdIdCached): Future[Boolean] =
    getMtdReference(reference.ninoHash).flatMap {
      case Some(_) =>
        Future.successful(true)
      case None =>
        collection
          .insertOne(reference)
          .toFuture()
          .map(_ => true)
          .recover {
            case e: DuplicateKeyException =>
              logger.warn("Duplicate key error, document already inserted", e)
              true
            case e =>
              logger.warn("Error saving reference to cache", e)
              false
          }
    }

  def dropCollection(): Future[Long] =
    collection.drop().toFuture().flatMap { _ =>
      collection.countDocuments().toFuture().map { count =>
        logger.warn(s"Collection dropped. Document count: $count")
        count
      }
    }

  def removeAll(): Future[DeleteResult] = collection.deleteMany(new BasicDBObject()).toFuture()

  def getMtdReference(ninoHash: String): Future[Option[MtdIdCached]] =
    collection
      .findOneAndUpdate(
        filter = equal("ninoHash", ninoHash),
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
