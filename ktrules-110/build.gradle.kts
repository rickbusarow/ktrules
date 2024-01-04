/*
 * Copyright (C) 2024 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  id("module")
  alias(libs.plugins.google.ksp)
}

jvmModule {
  // shadow()
  published(
    artifactId = "ktrules-110",
    pomDescription = "KtLint rules"
  )
}

dependencies {

  api(libs.ec4j.core)
  api(libs.jetbrains.markdown)
  api(libs.kotlin.reflect)
  api(libs.ktlint110.cli.ruleset.core)
  api(libs.ktlint110.rule.engine.core)

  compileOnly(libs.google.auto.service.annotations)
  compileOnly(libs.kotlin.compiler)

  ksp(libs.zacSweers.auto.service.ksp)

  api(project(":ktrules-api"))

  testCompileOnly(libs.google.auto.service.annotations)

  testImplementation(libs.jetbrains.markdown)
  testImplementation(libs.jimfs)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.common)
  testImplementation(libs.kotest.extensions)
  testImplementation(libs.kotest.property.jvm)
  testImplementation(libs.kotest.runner.junit5.jvm)
  testImplementation(libs.kotlin.compiler)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.ktlint110.cli.ruleset.core)
  testImplementation(libs.ktlint110.rule.engine)
  testImplementation(libs.ktlint110.rule.engine.core)
  testImplementation(libs.ktlint110.ruleset.standard)
  testImplementation(libs.ktlint110.test)
  testImplementation(libs.slf4j.api)
}
