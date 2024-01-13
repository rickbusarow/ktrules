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

@file:Suppress("PropertyName", "VariableNaming")

import builds.VERSION_NAME
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer

buildscript {
  dependencies {
    classpath(libs.rickBusarow.ktrules)
  }
}

plugins {
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doks)
  alias(libs.plugins.jetbrains.changelog)
  alias(libs.plugins.moduleCheck)
  id("root")
}

moduleCheck {
  deleteUnused = true
  checks {
    depths = true
    sortDependencies = true
  }
  reports {
    depths.enabled = true
    graphs {
      enabled = true
      outputDir = "${layout.buildDirectory.get().asFile}/reports/modulecheck/graphs"
    }
  }
}

val VERSION_CURRENT: String = VERSION_NAME
val VERSION_RELEASED = VERSION_CURRENT
  .takeIf { SemVer.from(it).preRelease.isNullOrEmpty() }
  ?: libs.versions.ktrules.released.get()
val GROUP = "com.rickbusarow.ktrules"
val GITHUB_REPOSITORY: String by project

doks {
  dokSet {
    docs("README.md", "CHANGELOG.md")

    sampleCodeSource.from(
      fileTree(projectDir.resolve("ktrules/src/test/kotlin")) {
        include("**/*.kt")
      }
    )

    rule("current-ktlint-version") {
      regex = "(current KtLint \\().*?(\\))"
      replacement = "$1${libs.versions.ktlint.lib.get()}$2"
    }

    rule("maven-artifact") {
      regex = maven(group = Regex.escape(GROUP))
      replacement = "$1:$2:$VERSION_RELEASED"
    }

    rule("editorconfig-sample") {
      replacement = sourceCode(
        fqName = "$GROUP.EditorConfigPropertiesTest.Environment.defaultConfig",
        bodyOnly = true,
        codeBlockLanguage = "editorconfig"
      )
    }
  }
}

changelog {
  version.set(VERSION_CURRENT)
  path.set(file("CHANGELOG.md").canonicalPath)
  header.set(provider { "[${version.get()}] - ${date()}" })
  itemPrefix.set("-")
  keepUnreleasedSection.set(true)
  groups.empty()
  lineSeparator.set("\n")
  combinePreReleases.set(true)

  repositoryUrl.set(GITHUB_REPOSITORY)
}
