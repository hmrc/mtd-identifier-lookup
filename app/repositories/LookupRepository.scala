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

import com.mongodb.BasicDBObject
import models.MtdIdReference
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.mongodb.scala.result.DeleteResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait LookupRepository {

  def save(nino: String, mtdId: String): Future[Boolean]

  def getMtdReference(nino: String): Future[Option[MtdIdReference]]

}

@Singleton
class LookupRepositoryImpl @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[MtdIdReference](
      collectionName = "mtdIdLookup",
      mongoComponent = mongo,
      domainFormat = MtdIdReference.format,
      indexes = Seq(
        IndexModel(ascending("nino"), IndexOptions().name("mtd-nino").unique(true).background(true))
      ),
      replaceIndexes = false
    )
    with LookupRepository {

  def save(nino: String, mtdId: String): Future[Boolean] =
    collection.insertOne(MtdIdReference(nino, mtdId)).toFuture().map(result => result.wasAcknowledged())

  def removeAll(): Future[DeleteResult] = collection.deleteMany(new BasicDBObject()).toFuture()

  def getMtdReference(nino: String): Future[Option[MtdIdReference]] =
    collection.find(equal("nino", nino)).toFuture().map(_.headOption)

}
