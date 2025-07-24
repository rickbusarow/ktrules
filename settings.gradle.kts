/*
 * Copyright (C) 2025 Rick Busarow
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
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

plugins {
  id("com.gradle.develocity") version "4.1"
}

val inGHA = !System.getenv("GITHUB_ACTIONS").isNullOrEmpty()

develocity {
  buildScan {

    uploadInBackground = true

    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"

    capture {
      testLogging = true
      buildLogging = true
      fileFingerprints = true
    }

    obfuscation {
      hostname { "<hostName>" }
      ipAddresses { listOf("<ip address>") }
      username { "<username>" }
    }

    tag(if (inGHA) "GitHub-Actions" else "Local")

    // publishing { onlyIf { true} }

    if (inGHA) {
      // ex: `octocat/Hello-World` as in github.com/octocat/Hello-World
      val repository = System.getenv("GITHUB_REPOSITORY")!!
      val runId = System.getenv("GITHUB_RUN_ID")!!

      link(
        "GitHub Action Run",
        "https://github.com/$repository/actions/runs/$runId"
      )
    }
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
  }
}

include("lib")

if (inGHA) {
  fun Long.gigabytes(): String {
    return "%.1f GB".format(toDouble() / (1024 * 1024 * 1024)).padEnd(52)
  }

  val os = System.getProperty("os.name").toString().padEnd(52)
  val processors = Runtime.getRuntime().availableProcessors().toString().padEnd(52)
  val totalMemory = Runtime.getRuntime().totalMemory().gigabytes()
  val freeMemory = Runtime.getRuntime().freeMemory().gigabytes()
  val maxMemory = Runtime.getRuntime().maxMemory().gigabytes()
  println(
    """
    ╔══════════════════════════════════════════════════╗
    ║                  CI environment                  ║
    ║                                                  ║
    ║                     OS - $os                     ║
    ║   available processors - $processors             ║
    ║                                                  ║
    ║       allocated memory - $totalMemory            ║
    ║            free memory - $freeMemory             ║
    ║             max memory - $maxMemory              ║
    ╚══════════════════════════════════════════════════╝
    """.trimIndent()
      .lineSequence()
      .joinToString("\n") { line ->
        val sub = line.substringBeforeLast('║', "")
        if (sub.length > 51) sub.substring(0..50) + '║' else line
      }
  )
}
