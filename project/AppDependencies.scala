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

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrap_30_version = "8.6.0"
  val mongoPlayVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrap_30_version,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.16.1",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % mongoPlayVersion,
    "uk.gov.hmrc"                  %% "crypto-json-play-30"       % "8.2.0"
  )

  def test(scope: String = "test, it"): Seq[sbt.ModuleID] = Seq(
    "org.scalatest"       %% "scalatest"               % "3.2.18"             % scope,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.6"             % scope,
    "org.playframework"   %% "play-test"               % PlayVersion.current  % scope,
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrap_30_version % scope,
    "org.scalamock"       %% "scalamock"               % "5.2.0"              % scope,
    "org.wiremock"         % "wiremock"                % "3.3.1"              % scope,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % mongoPlayVersion     % scope
  )

}
