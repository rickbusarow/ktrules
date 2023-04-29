/*
 * Copyright (C) 2023 Rick Busarow
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

@file:Suppress("PropertyName", "VariableNaming")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform")
}

val ktlintVersionAttribute: Attribute<String> = Attribute.of(
  "com.rickbusarow.ktrules.ktlintVersion",
  String::class.java
)

kotlin {
  jvm()
  jvm("ktlint47") {
    attributes.attribute(ktlintVersionAttribute, "ktlint47")
  }
  jvm("ktlint48") {
    attributes.attribute(ktlintVersionAttribute, "ktlint48")
  }
  jvm("ktlint49") {
    attributes.attribute(ktlintVersionAttribute, "ktlint49")
  }
  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      allWarningsAsErrors = false

      val kotlinMajor = libs.versions.kotlinApi.get()
      languageVersion = kotlinMajor
      apiVersion = kotlinMajor

      jvmTarget = libs.versions.jvmTarget.get()

      freeCompilerArgs += listOf(
        "-opt-in=kotlin.contracts.ExperimentalContracts"
      )
    }
  }
}

val jvmMain by kotlin.sourceSets.getting {
  dependencies {
    api(libs.ec4j.core)
    api(libs.jetbrains.markdown)

    compileOnly(libs.google.auto.service.annotations)
    compileOnly(libs.kotlin.compiler)
  }
}
val ktlint47Main by kotlin.sourceSets.getting {
  dependsOn(jvmMain)
  dependencies {
    api((libs.jetbrains.markdown))
    api(libs.ktlint47.core)

    compileOnly(libs.google.auto.service.annotations)
  }
}
val ktlint48Main by kotlin.sourceSets.getting {
  dependsOn(jvmMain)
  dependencies {
    api(libs.ec4j.core)
    api(libs.jetbrains.markdown)
    api(libs.ktlint48.core)

    compileOnly(libs.google.auto.service.annotations)
  }
}
val ktlint49Main by kotlin.sourceSets.getting {
  dependsOn(jvmMain)
  dependencies {
    api(libs.ec4j.core)
    api(libs.jetbrains.markdown)
    api(libs.ktlint.cli.ruleset.core)
    api(libs.ktlint.rule.engine.core)

    compileOnly(libs.google.auto.service.annotations)
  }
}
