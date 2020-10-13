/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "mtd-identifier-lookup"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    dependencyOverrides ++= AppDependencies.overrides,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalaVersion := "2.12.10"
  )
  .settings(publishingSettings: _*)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(defaultSettings(): _*)
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.itSettings): _*)
  .settings(
    fork in ItTest := true,
    unmanagedSourceDirectories in ItTest := Seq((baseDirectory in ItTest).value / "it"),
    unmanagedClasspath in ItTest += baseDirectory.value / "resources",
    unmanagedClasspath in Runtime += baseDirectory.value / "resources",
    javaOptions in ItTest += "-Dlogger.resource=logback-test.xml",
    parallelExecution in ItTest := false,
    addTestReportOption(ItTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 9769)
  .settings(
    // from https://github.com/playframework/playframework/issues/7382#issuecomment-444062750
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )

dependencyUpdatesFilter -= moduleFilter(organization = "com.typesafe.play")
dependencyUpdatesFilter -= moduleFilter(name = "simple-reactivemongo")
dependencyUpdatesFilter -= moduleFilter(name = "reactivemongo-test")
dependencyUpdatesFilter -= moduleFilter(name = "domain")
dependencyUpdatesFilter -= moduleFilter(name = "scala-library")
dependencyUpdatesFilter -= moduleFilter(name = "flexmark-all")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-play")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-scalacheck")
dependencyUpdatesFailBuild := true
