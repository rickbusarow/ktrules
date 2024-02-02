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

rootProject.name = "ktrules"

pluginManagement {
  repositories {
    val allowMavenLocal = providers
      .gradleProperty("${rootProject.name}.allow-maven-local")
      .orNull.toBoolean()
    if (allowMavenLocal) {
      logger.lifecycle("${rootProject.name} -- allowing mavenLocal for plugins")
      mavenLocal()
    }
    gradlePluginPortal()
    mavenCentral()
    google()
  }

  includeBuild("build-logic")

  plugins {
    id("module") apply false
    id("root") apply false
  }
}

plugins {
  id("com.gradle.enterprise") version "3.16.2"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    val allowMavenLocal = providers
      .gradleProperty("${rootProject.name}.allow-maven-local")
      .orNull.toBoolean()

    if (allowMavenLocal) {
      logger.lifecycle("${rootProject.name} -- allowing mavenLocal for dependencies")
      mavenLocal()
    }
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
  }
}

include(":ktrules")
