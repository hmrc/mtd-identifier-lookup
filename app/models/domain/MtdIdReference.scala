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

package models.domain

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, JsPath, Json, OFormat, Reads, Writes}

//Note: as saved in MongoDB
case class MtdIdMongoReference(nino: String, mtdRef: String)

object MtdIdMongoReference {
  implicit val format: OFormat[MtdIdMongoReference]                      = Json.format[MtdIdMongoReference]
  def toIdReference(mongoReference: MtdIdMongoReference): MtdIdReference = MtdIdReference( mongoReference.mtdRef)
}

//Note: fleshed this out to include full reads and writes
case class MtdIdReference(mtdbsa: String)

object MtdIdReference {
//NOTE: confirm DES Mapping (mtdbsa) is correct
  implicit val reads: Reads[MtdIdReference] = (
    (JsPath \ "mtdRef").read[String] orElse
      (JsPath \ "taxPayerDisplayResponse" \ "mtdId").read[String]
  ).map(MtdIdReference.apply _)

  // NOTE: Serialising to JSON as returned to the client
  implicit val writes: Writes[MtdIdReference] =
    (JsPath \ "mtdbsa").write[String].contramap(unlift(MtdIdReference.unapply))

  implicit val format: Format[MtdIdReference] =
    Format(reads, writes)

}
