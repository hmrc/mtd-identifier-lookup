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

package repositories

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import models.MtdIdReference
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[LookupRepositoryImpl])
trait LookupRepository {

  def save(nino: String, mtdId: String)(implicit hc: HeaderCarrier,
                                        ec: ExecutionContext): Future[Boolean]

  def getMtdReference(nino: String)(implicit hc: HeaderCarrier,
                                    ec: ExecutionContext): Future[Option[MtdIdReference]]

}

@Singleton
class LookupRepositoryImpl (implicit mongo: () => DB)
  extends ReactiveRepository[MtdIdReference, BSONObjectID]("mtdIdLookup",
                                                            mongo,
                                                            MtdIdReference.format,
                                                            ReactiveMongoFormats.objectIdFormats) with LookupRepository{

  override def indexes: Seq[Index] = Seq(Index(Seq(("nino", Ascending)), name = Some("mtd-nino"), unique = true))

  override def save(nino: String, mtdId: String)(implicit hc: HeaderCarrier,
                                                 ec: ExecutionContext): Future[Boolean] = {
    insert(MtdIdReference(nino, mtdId)).map(result => result.ok && result.n > 0)
  }

  override def getMtdReference(nino: String)(implicit hc: HeaderCarrier,
                                             ec: ExecutionContext): Future[Option[MtdIdReference]] = {
    find("nino" -> nino).map(_.headOption)
  }
}

