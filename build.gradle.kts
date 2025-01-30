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

@file:Suppress("PropertyName", "VariableNaming")

import com.diffplug.gradle.spotless.GroovyGradleExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.rickbusarow.doks.DoksTask
import com.rickbusarow.ktlint.KtLintTask
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.jetbrains.changelog.date
import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.text.RegexOption.MULTILINE

buildscript {
  dependencies {
    classpath(libs.rickBusarow.ktrules)
  }
}

plugins {
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.detekt)
  alias(libs.plugins.doks)
  alias(libs.plugins.dokka)
  alias(libs.plugins.github.release)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.kotlinx.binaryCompatibility)
  alias(libs.plugins.jetbrains.changelog)
  alias(libs.plugins.moduleCheck)
  alias(libs.plugins.spotless)
  id("maven-publish")
  id("signing")
}

val VERSION_CURRENT = libs.versions.ktrules.dev.get()
val VERSION_RELEASED = VERSION_CURRENT
  .takeIf { SemVer.from(it).preRelease.isNullOrEmpty() }
  ?: libs.versions.ktrules.released.get()
val GROUP = "com.rickbusarow.ktrules"
val website = "https://www.github.com/rbusarow/ktrules/"

doks {
  dokSet {
    docs("README.md", "CHANGELOG.md")

    sampleCodeSource.from(
      fileTree(projectDir.resolve("src/test/kotlin")) {
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

tasks.withType<DoksTask>().configureEach {
  mustRunAfter(tasks.withType<KotlinCompile>())
  mustRunAfter("apiDump")
}

val updateEditorConfigVersion by tasks.registering {

  val file = file(".editorconfig")
  val current = VERSION_CURRENT

  doLast {
    val oldText = file.readText()

    val reg = """^(ktlint_kt-rules_project_version *?= *?)\S*$""".toRegex(MULTILINE)

    val newText = oldText.replace(reg, "$1$current")

    if (newText != oldText) {
      file.writeText(newText)
    }
  }
}

tasks.withType<KtLintTask>().configureEach {
  dependsOn(updateEditorConfigVersion)

  mustRunAfter(
    tasks.matching {
      it.name == "dependencyGuard" || it.name == "dependencyGuardBaseline" || it.name == "apiDump"
    },
    tasks.withType(KotlinApiBuildTask::class.java),
    tasks.withType(KotlinApiCompareTask::class.java)
  )
}

val fix by tasks.registering {

  group = "Verification"
  description = "Runs all auto-fix linting tasks"

  dependsOn("apiDump")
  dependsOn("doks")
  dependsOn("ktlintFormat")
  dependsOn("spotlessApply")
  dependsOn("moduleCheckAuto")
}

// This is a convenience task which applies all available fixes before running `check`. Each
// of the fixable linters use `mustRunAfter` to ensure that their auto-fix task runs before their
// check-only task.
val checkFix by tasks.registering {
  group = "Verification"
  description = "Runs all auto-fix linting tasks, then runs all of the normal :check task"

  dependsOn(tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME))
  dependsOn(fix)
}

val checkVersionIsSnapshot by tasks.registering {
  group = "publishing"
  description = "ensures that the project version has a -SNAPSHOT suffix"
  doLast {
    val expected = "-SNAPSHOT"
    require(VERSION_CURRENT.endsWith(expected)) {
      "The project's version name must be suffixed with `$expected` when checked in" +
        " to the main branch, but instead it's `$VERSION_CURRENT`."
    }
  }
}

val checkVersionIsNotSnapshot by tasks.registering {
  group = "publishing"
  description = "ensures that the project version does not have a -SNAPSHOT suffix"
  doLast {
    require(!VERSION_CURRENT.endsWith("-SNAPSHOT")) {
      "The project's version name cannot have a -SNAPSHOT suffix, but it was $VERSION_CURRENT."
    }
  }
}

tasks.withType<SpotlessTask>().configureEach {
  mustRunAfter("apiDump")
}

spotless {
  format("markdown") {
    target(
      fileTree(projectDir) {
        include("**/*.md")
        include("**/*.mdx")

        exclude("**/.docusaurus/**")
        exclude("**/build/**")
        exclude("**/dokka-archive/**")
        exclude("**/node_modules/**")
        exclude("website/static/api/**")
        exclude("artifacts.json")
        exclude(".gradle/**")
        exclude(".git/**")
      }
    )

    prettier(libs.versions.prettier.lib.get())

    withinBlocksRegex(
      "groovy block in markdown",
      //language=regexp
      """```groovy.*\n((?:(?!```)[\s\S])*)""",
      GroovyGradleExtension::class.java
    ) {
      greclipse()
      indentWithSpaces(2)
    }
  }
}

githubRelease {

  val versionString = VERSION_CURRENT

  token {
    property("GITHUB_PERSONAL_ACCESS_TOKEN") as? String
      ?: throw GradleException(
        "In order to release, you must provide a GitHub Personal Access Token " +
          "as a property named 'GITHUB_PERSONAL_ACCESS_TOKEN'."
      )
  }
  owner.set("rbusarow")

  tagName.set(versionString)
  releaseName.set(versionString)

  generateReleaseNotes.set(false)

  body.set(
    provider {
      if (versionString.endsWith("-SNAPSHOT")) {
        throw GradleException(
          "do not create a GitHub release for a snapshot. (version is $versionString)."
        )
      }
      val escapedVersion = Regex.escape(versionString)
      val dateSuffixRegex = """ +- +\d{4}-\d{2}-\d{2}.*""".toRegex()
      val currentVersionRegex = """(?:^|\n)## \[$escapedVersion]$dateSuffixRegex""".toRegex()
      val lastVersionRegex = """## \[(.*?)]$dateSuffixRegex""".toRegex()
      // capture everything in between '## [<this version>]' and a new line which starts with '## '
      val versionSectionRegex =
        """$currentVersionRegex\n([\s\S]*?)(?=\n+$lastVersionRegex)""".toRegex()
      versionSectionRegex
        .find(file("CHANGELOG.md").readText())
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?.also { body ->
          if (body.isBlank()) {
            throw GradleException("The changelog for this version cannot be blank.")
          }
        }
        ?: throw GradleException(
          "could not find a matching change log for $versionSectionRegex"
        )
    }
  )

  overwrite.set(false)
  dryRun.set(false)
  draft.set(false)
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

  repositoryUrl.set(website)
}
