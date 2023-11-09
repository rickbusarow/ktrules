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

import com.diffplug.gradle.spotless.GroovyGradleExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.rickbusarow.doks.DoksTask
import com.rickbusarow.ktlint.KtLintTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import modulecheck.utils.capitalize
import org.jetbrains.changelog.date
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import kotlin.text.RegexOption.MULTILINE

buildscript {
  dependencies {
    classpath(libs.rickBusarow.ktrules)
  }
  configurations.classpath {
    exclude(group = "org.gradle")
  }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.detekt)
  alias(libs.plugins.doks)
  alias(libs.plugins.dokka)
  alias(libs.plugins.github.release)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.kotlinx.binaryCompatibility)
  alias(libs.plugins.jetbrains.changelog)
  alias(libs.plugins.moduleCheck)
  alias(libs.plugins.spotless)
  id("maven-publish")
  id("signing")
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

val compatSourceSetNames = listOf(
  "compat47",
  "compat48",
  "compat49",
  "compat50",
  "compat100"
)

compatSourceSetNames.forEach { ssName ->

  sourceSets.register(ssName) {
    java.srcDir("src/$ssName/kotlin")
    resources.srcDir("src/$ssName/resources")
    compileClasspath += sourceSets["main"].output

    runtimeClasspath += output + compileClasspath
  }

  tasks.register("${ssName}Jar", Jar::class.java) {
    from(sourceSets[ssName].output)
    from(sourceSets["main"].output)
    archiveFileName.set("$ssName.jar")
  }

  tasks.register("${ssName}SourcesJar", Jar::class.java) {
    archiveClassifier.set("sources")
    from(sourceSets[ssName].allSource)
    from(sourceSets["main"].allSource)
  }
}

val compat47Api: Configuration by configurations.getting
val compat48Api: Configuration by configurations.getting
val compat49Api: Configuration by configurations.getting
val compat50Api: Configuration by configurations.getting

val compat100: SourceSet by sourceSets.getting
val compat100Implementation: Configuration by configurations.getting
val compat100Api: Configuration by configurations.getting

sourceSets.named("test") {
  compileClasspath += compat100.output
  runtimeClasspath += output + compileClasspath
}

dependencies {

  api(libs.ec4j.core)
  api(libs.jetbrains.markdown)
  api(libs.kotlin.reflect)

  compat100Api(libs.jetbrains.markdown)
  compat100Api(libs.ktlint.cli.ruleset.core)
  compat100Api(libs.ktlint.rule.engine.core)
  compat47Api(libs.jetbrains.markdown)
  compat47Api(libs.ktlint47.core)
  compat48Api(libs.ec4j.core)
  compat48Api(libs.jetbrains.markdown)
  compat48Api(libs.ktlint48.core)
  compat49Api(libs.ec4j.core)
  compat49Api(libs.jetbrains.markdown)
  compat49Api(libs.ktlint49.cli.ruleset.core)
  compat49Api(libs.ktlint49.rule.engine.core)
  compat50Api(libs.jetbrains.markdown)
  compat50Api(libs.ktlint.cli.ruleset.core)
  compat50Api(libs.ktlint.rule.engine.core)

  "compat100CompileOnly"(libs.google.auto.service.annotations)
  "compat47CompileOnly"(libs.google.auto.service.annotations)
  "compat48CompileOnly"(libs.google.auto.service.annotations)
  "compat49CompileOnly"(libs.google.auto.service.annotations)
  "compat50CompileOnly"(libs.google.auto.service.annotations)

  compileOnly(libs.google.auto.service.annotations)
  compileOnly(libs.kotlin.compiler)

  detektPlugins(libs.detekt.rules.libraries)

  ksp(libs.zacSweers.auto.service.ksp)

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
  testImplementation(libs.ktlint.cli.ruleset.core)
  testImplementation(libs.ktlint.rule.engine)
  testImplementation(libs.ktlint.rule.engine.core)
  testImplementation(libs.ktlint.ruleset.standard)
  testImplementation(libs.ktlint.test)
  testImplementation(libs.slf4j.api)
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

tasks.named("apiCheck") { mustRunAfter("apiDump") }

val updateEditorConfigVersion by tasks.registering {

  val file = file(".editorconfig")

  doLast {
    val oldText = file.readText()

    val reg = """^(ktlint_kt-rules_project_version *?= *?)\S*$""".toRegex(MULTILINE)

    val newText = oldText.replace(reg, "$1$VERSION_CURRENT")

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

tasks.test {
  useJUnitPlatform()

  // Illegal reflective operation warnings while KtLint formats.  It's a Kotlin issue.
  // https://github.com/pinterest/ktlint/issues/1618
  jvmArgs(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED"
  )

  systemProperties.putAll(
    mapOf(
      // remove parentheses from test display names
      "junit.jupiter.displayname.generator.default" to
        "org.junit.jupiter.api.DisplayNameGenerator\$Simple",

      // https://junit.org/junit5/docs/snapshot/user-guide/#writing-tests-parallel-execution-config-properties
      // Allow unit tests to run in parallel
      "junit.jupiter.execution.parallel.enabled" to true,
      "junit.jupiter.execution.parallel.mode.default" to "concurrent",
      "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",

      "junit.jupiter.execution.parallel.config.strategy" to "dynamic",
      "junit.jupiter.execution.parallel.config.dynamic.factor" to 1.0
    )
  )

  maxParallelForks = Runtime.getRuntime().availableProcessors()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
  }

  plugins.withType<PublishingPlugin>().configureEach {
    extensions.configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    tasks.withType<JavaCompile>().configureEach {
      options.release.set(libs.versions.jvmTarget.get().substringAfterLast('.').toInt())
    }
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

val buildTests by tasks.registering { dependsOn("testClasses") }
val buildAll by tasks.registering {
  dependsOn(provider { sourceSets.map { it.classesTaskName } })
}

// fixes the error
// 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
// when executing a Jar task
// https://github.com/gradle/gradle/issues/17236
tasks.withType<Jar>().configureEach {
  // duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val deleteEmptyDirs by tasks.registering(Delete::class) {
  description = "Delete all empty directories within a project."
  doLast {

    val subprojectDirs = subprojects.map { it.projectDir.path }

    projectDir.walkBottomUp()
      .filter { it.isDirectory }
      .filterNot { dir -> subprojectDirs.any { dir.path.startsWith(it) } }
      .filterNot { it.path.contains(".gradle") }
      .filterNot { it.path.contains(".git") }
      .filter { it.listFiles().isNullOrEmpty() }
      .forEach { it.deleteRecursively() }
  }
}

tasks.named(LifecycleBasePlugin.CLEAN_TASK_NAME) { dependsOn(deleteEmptyDirs) }

val detektExcludes = listOf(
  "**/resources/**",
  "**/build/**"
)

extensions.configure<DetektExtension> {

  autoCorrect = false
  config.from("$projectDir/detekt/detekt.yml")
  buildUponDefaultConfig = true

  source.from(
    "src/main/java",
    "src/test/java",
    "src/main/kotlin",
    "src/test/kotlin"
  )

  parallel = true
}

tasks.withType<Detekt>().configureEach {

  autoCorrect = false
  parallel = true
  config.from("$projectDir/detekt/detekt.yml")
  buildUponDefaultConfig = true

  reports {
    xml.required.set(true)
    html.required.set(true)
    txt.required.set(false)
    sarif.required.set(true)
  }

  exclude(detektExcludes)

  // https://github.com/detekt/detekt/issues/4127
  exclude { "/build/generated/" in it.file.absolutePath }

  mustRunAfter("ktlintFormat")
}

fun otherDetektTasks(targetTask: Task, withAutoCorrect: Boolean): TaskCollection<Detekt> {
  return tasks.withType<Detekt>()
    .matching { it.autoCorrect == withAutoCorrect && it != targetTask }
}

val detektAll by tasks.registering(Detekt::class) {
  description = "runs the standard PSI Detekt as well as all type resolution tasks"
  dependsOn(otherDetektTasks(targetTask = this@registering, withAutoCorrect = false))
}

// Make all tasks from Detekt part of the 'detekt' task group.  Default is 'verification'.
sequenceOf(
  Detekt::class.java,
  DetektCreateBaselineTask::class.java,
  DetektGenerateConfigTask::class.java
).forEach { type ->
  tasks.withType(type).configureEach { group = "detekt" }
}

// By default, `check` only handles the PSI Detekt task.  This adds the type resolution tasks.
tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
  dependsOn(otherDetektTasks(targetTask = this@named, withAutoCorrect = false))
}

val fix by tasks.registering {

  group = "Verification"
  description = "Runs all auto-fix linting tasks"

  dependsOn("apiDump")
  dependsOn("doks")
  dependsOn("ktlintFormat")
  dependsOn("spotlessApply")
  dependsOn("moduleCheckAuto")
  dependsOn(deleteEmptyDirs)
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

tasks.withType(PublishToMavenRepository::class.java).configureEach {
  notCompatibleWithConfigurationCache("See https://github.com/gradle/gradle/issues/13468")
  mustRunAfter(tasks.withType(Sign::class.java))
}
tasks.withType(Jar::class.java).configureEach {
  notCompatibleWithConfigurationCache("")
}
tasks.withType(Sign::class.java).configureEach {
  notCompatibleWithConfigurationCache("")
  // skip signing for -SNAPSHOT publishing
  onlyIf { !VERSION_CURRENT.endsWith("SNAPSHOT") }
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
  val task = tasks.named("dokkaHtml")
  archiveClassifier.set("javadoc")
  dependsOn(task)
  from(task)
}

publishing {
  publications.withType(MavenPublication::class.java).configureEach {

    groupId = GROUP
    version = VERSION_CURRENT

    pom {
      description.set("ktlint rules")
      url.set(website)

      licenses {
        license {
          name.set("The Apache Software License, Version 2.0")
          url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          distribution.set("repo")
        }
      }
      scm {
        url.set(website)
        connection.set("scm:git:git://github.com/rbusarow/ktrules.git")
        developerConnection.set("scm:git:ssh://git@github.com/rbusarow/ktrules.git")
      }
      developers {
        developer {
          id.set("rbusarow")
          name.set("Rick Busarow")
          url.set("https://github.com/rbusarow/")
        }
      }
    }
  }

  compatSourceSetNames
    .forEachIndexed { index, sourceSetName ->
      val jarTask = tasks.named("${sourceSetName}Jar", Jar::class.java)
      val sourcesJar = tasks.named("${sourceSetName}SourcesJar", Jar::class.java)

      publications {
        register(sourceSetName, MavenPublication::class.java) {

          artifact(jarTask)
          artifact(sourcesJar)
          artifact(dokkaJavadocJar)

          artifactId = if (index == compatSourceSetNames.lastIndex) {
            // don't include the `-4x` suffix for the latest version
            "ktrules"
          } else {
            "ktrules-${sourceSetName.substringAfter("compat")}"
          }

          // configure the signing
          signing {
            sign(this@register)
          }

          pom.name.set(artifactId)

          pom.withXml {

            (
              (asNode().get("dependencies") as groovy.util.NodeList).firstOrNull() as? groovy.util.Node
                ?: asNode().appendNode("dependencies")
              )
              .apply {
                val ss: SourceSet = sourceSets[sourceSetName]
                listOf(
                  ss.implementationConfigurationName,
                  ss.runtimeOnlyConfigurationName,
                  ss.apiConfigurationName
                )
                  .flatMap { configurations[it].allDependencies }
                  .distinct()
                  .forEach { dep ->

                    appendNode("dependency").also { depNode ->
                      depNode.appendNode("groupId", dep.group)
                      depNode.appendNode("artifactId", dep.name)
                      depNode.appendNode("version", dep.version)
                      depNode.appendNode("scope", "runtime")
                    }
                  }
              }
          }
        }
      }

      tasks.withType(AbstractPublishToMaven::class.java)
        .matching { it.name.contains(sourceSetName.capitalize()) }
        .configureEach {
          dependsOn(jarTask)
          dependsOn(sourcesJar)
          dependsOn(dokkaJavadocJar)
        }
      tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        mustRunAfter(tasks.withType(Jar::class.java))
      }
      tasks.withType(GenerateModuleMetadata::class.java).configureEach {
        mustRunAfter(tasks.withType(Jar::class.java))
      }
      tasks.withType(Sign::class.java).configureEach {
        dependsOn(jarTask)
        dependsOn(sourcesJar)
        dependsOn(dokkaJavadocJar)
      }
    }

  repositories.maven {
    name = "mavenCentral"
    if (VERSION_CURRENT.endsWith("-SNAPSHOT")) {
      setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    } else {
      setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
    }

    credentials {
      username = project.findProperty("mavenCentralUsername") as? String
        ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername")
      password = project.findProperty("mavenCentralPassword") as? String
        ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralPassword")
    }
  }
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

tasks.withType(AbstractDokkaLeafTask::class.java).configureEach {

  // Dokka doesn't support configuration caching
  notCompatibleWithConfigurationCache("Dokka doesn't support configuration caching")

  // Dokka uses their outputs but doesn't explicitly depend upon them.
  mustRunAfter(tasks.withType(KotlinCompile::class.java))
  mustRunAfter(tasks.withType(KtLintTask::class.java))

  moduleName.set("ktrules")

  dokkaSourceSets.configureEach sourceSets@{

    val dokkaSourceSet: GradleDokkaSourceSetBuilder = this@sourceSets

    dokkaSourceSet.suppress.set(
      dokkaSourceSet.name != "main" && dokkaSourceSet.name !in compatSourceSetNames
    )

    documentedVisibilities.set(
      setOf(
        DokkaConfiguration.Visibility.PUBLIC,
        DokkaConfiguration.Visibility.PRIVATE,
        DokkaConfiguration.Visibility.PROTECTED,
        DokkaConfiguration.Visibility.INTERNAL,
        DokkaConfiguration.Visibility.PACKAGE
      )
    )

    languageVersion.set(libs.versions.kotlinApi.get())

    // include all project sources when resolving kdoc samples
    samples.setFrom(fileTree(file("src")))

    sourceLink {
      localDirectory.set(file("src/${dokkaSourceSet.name}"))

      val modulePath = path.replace(":", "/")
        .replaceFirst("/", "")

      // URL showing where the source code can be accessed through the web browser
      remoteUrl.set(
        URL("https://github.com/rbusarow/ktrules/blob/main/$modulePath/src/main")
      )
      // Suffix which is used to append the line number to the URL. Use #L for GitHub
      remoteLineSuffix.set("#L")
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
