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

import com.vanniktech.maven.publish.tasks.JavadocJar
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
  id("module")
  alias(libs.plugins.google.ksp)
  idea
}

jvmModule {
  poko()
  published(
    artifactId = "ktrules-api",
    pomDescription = "KtLint rules"
  )

  tasks.register("testAll", Test::class.java) { group = "Verification" }

  featureVariants {
    variant("compat47") {
      api(libs.ktlint47.ruleset.standard)
      api(libs.ktlint47.core)
      testImplementation(libs.ktlint47.test)
    }
    variant("compat48") {
      api(libs.ktlint48.core)
      testImplementation(libs.ktlint48.test)
    }
    variant("compat49") {
      api(libs.ktlint49.cli.ruleset.core)
      api(libs.ktlint49.rule.engine.core)
      testImplementation(libs.ktlint49.cli.ruleset.core)
      testImplementation(libs.ktlint49.rule.engine)
      testImplementation(libs.ktlint49.rule.engine.core)
      testImplementation(libs.ktlint49.test)
    }
    variant("compat50") {
      api(libs.ktlint50.cli.ruleset.core)
      api(libs.ktlint50.rule.engine.core)
      testImplementation(libs.ktlint50.cli.ruleset.core)
      testImplementation(libs.ktlint50.rule.engine)
      testImplementation(libs.ktlint50.rule.engine.core)
      testImplementation(libs.ktlint50.test)
    }
    variant("compat100") {
      api(libs.ktlint100.cli.ruleset.core)
      api(libs.ktlint100.rule.engine.core)
      testImplementation(libs.ktlint100.cli.ruleset.core)
      testImplementation(libs.ktlint100.rule.engine)
      testImplementation(libs.ktlint100.rule.engine.core)
      testImplementation(libs.ktlint100.test)
    }
    variant("compat110") {
      api(libs.ktlint110.cli.ruleset.core)
      api(libs.ktlint110.rule.engine.core)
      testImplementation(libs.ktlint110.cli.ruleset.core)
      testImplementation(libs.ktlint110.rule.engine)
      testImplementation(libs.ktlint110.rule.engine.core)
      testImplementation(libs.ktlint110.test)
    }
  }
}

val test: SourceSet by sourceSets.getting
val main: SourceSet by sourceSets.getting

val kotlinMain: KotlinSourceSet by kotlin.sourceSets.named("main")
val kotlinMainTest: KotlinSourceSet by kotlin.sourceSets.named("test")

val compilations = kotlin.target.compilations

val kotlinMainCompilation by compilations.named("main")
val kotlinMainTestCompilation by compilations.named("test")

val dokkaJavadocJar = tasks.named("dokkaJavadocJar", JavadocJar::class)

// val compileCompat by tasks.registering {
//   dependsOn(compats.map(Compat::compileKotlinTask))
// }
// val compileCompatTest by tasks.registering {
//   dependsOn(compats.map(Compat::compileTestKotlinTask))
// }

dependencies {

  api(libs.ec4j.core)
  api(libs.jetbrains.markdown)
  api(libs.kotlin.reflect)

  compileOnly(libs.google.auto.service.annotations)
  compileOnly(libs.kotlin.compiler)

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
  testImplementation(libs.rickBusarow.kase)
  testImplementation(libs.slf4j.api)

  ksp(libs.zacSweers.auto.service.ksp)

  jvmModule.featureVariants.variants.configureEach variant@{
    val variant = this@variant
    variant.compileOnlyConfig.dependencies.addLater(libs.google.auto.service.annotations)
    variant.kspConfig.dependencies.addLater(libs.google.auto.service.annotations)
    variant.testImplementationConfig.dependencies.addLater(libs.rickBusarow.kase)
    variant.testImplementationConfig.dependencies.addLater(libs.jimfs)
  }
}
