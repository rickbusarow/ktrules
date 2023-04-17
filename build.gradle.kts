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
import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.rickbusarow.doks.DoksTask
import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.ec4j.core.Cache.Caches
import org.ec4j.core.PropertyTypeRegistry
import org.ec4j.core.Resource.Resources
import org.ec4j.core.ResourcePath.ResourcePaths
import org.ec4j.core.ResourcePropertiesService
import org.ec4j.core.model.EditorConfig
import org.ec4j.core.model.Version
import org.ec4j.core.parser.EditorConfigModelHandler
import org.ec4j.core.parser.EditorConfigParser
import org.ec4j.core.parser.ErrorHandler
import org.jetbrains.changelog.date
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.text.RegexOption.MULTILINE

buildscript {
  dependencies {
    classpath(libs.rickBusarow.ktrules)
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
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.kotlinx.binaryCompatibility)
  alias(libs.plugins.jetbrains.changelog)
  // alias(libs.plugins.shadowJar)
  alias(libs.plugins.spotless)
  alias(libs.plugins.vanniktech.publish)
}

dependencies {

  compileOnly(libs.google.auto.service.annotations)
  compileOnly(libs.kotlin.compiler)

  implementation(libs.ktlint.core)

  implementation(libs.jetbrains.markdown)
  implementation(libs.ec4j.core)

  detektPlugins(libs.detekt.rules.libraries)

  ksp(libs.zacSweers.auto.service.ksp)

  testCompileOnly(libs.google.auto.service.annotations)

  testImplementation(libs.jetbrains.markdown)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.common)
  testImplementation(libs.kotest.runner.junit5.jvm)
  testImplementation(libs.kotest.extensions)
  testImplementation(libs.kotest.property.jvm)
  testImplementation(libs.kotlin.compiler)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.ktlint.core)
  testImplementation(libs.ktlint.ruleset.standard)
  testImplementation(libs.ktlint.test)
  testImplementation(libs.slf4j.api)
}

val VERSION_CURRENT = libs.versions.ktrules.dev.get()
val VERSION_RELEASED = libs.versions.ktrules.released.get()
val GROUP = "com.rickbusarow.ktrules"
val website = "https://www.github.com/rbusarow/ktrules/"

doks {
  dokSet {
    docs("README.md", "CHANGELOG.md")

    sampleCodeSource.from(fileTree(projectDir.resolve("src/test/kotlin")) {
      include("**/*.kt")
    })

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

tasks.withType<DoksTask> {
  mustRunAfter(tasks.withType<KotlinCompile>())
  mustRunAfter("apiDump")
}

tasks.named("apiCheck") { mustRunAfter("apiDump") }

// dummy ktlint-gradle plugin task names which just delegate to the Kotlinter ones
val ktlintCheck by tasks.registering { dependsOn("lintKotlin") }
val ktlintFormat by tasks.registering { dependsOn("formatKotlin") }

tasks.named("lintKotlin") { mustRunAfter("formatKotlin") }

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

tasks.withType<ConfigurableKtLintTask> {
  source(buildFile)

  source(file("settings.gradle.kts"))

  dependsOn(updateEditorConfigVersion)
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

  plugins.withType<MavenPublishBasePlugin> {
    extensions.configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    tasks.withType<JavaCompile> {
      options.release.set(libs.versions.jvmTarget.get().substringAfterLast('.').toInt())
    }
  }

  tasks.withType<KotlinCompile> {
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

// fixes the error
// 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
// when executing a Jar task
// https://github.com/gradle/gradle/issues/17236
tasks.withType<Jar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
  config = files("$projectDir/detekt/detekt.yml")
  buildUponDefaultConfig = true

  source = files(
    "src/main/java",
    "src/test/java",
    "src/main/kotlin",
    "src/test/kotlin"
  )

  parallel = true
}

tasks.withType<Detekt> {

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

fun otherDetektTasks(
  targetTask: Task,
  withAutoCorrect: Boolean
): TaskCollection<Detekt> {
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
  tasks.withType(type) { group = "detekt" }
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
  dependsOn("formatKotlin")
  dependsOn("spotlessApply")
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

@Suppress("UnstableApiUsage")
configure<MavenPublishBaseExtension> {

  coordinates(
    groupId = GROUP,
    artifactId = "ktrules",
    version = VERSION_CURRENT
  )

  publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)

  signAllPublications()

  pom {
    description.set("ktlint rules")
    name.set("ktrules")

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

  configure(KotlinJvm(javadocJar = Dokka(taskName = "dokkaHtml"), sourcesJar = true))
}

tasks.withType(PublishToMavenRepository::class.java) {
  notCompatibleWithConfigurationCache("See https://github.com/gradle/gradle/issues/13468")
}
tasks.withType(Jar::class.java) {
  notCompatibleWithConfigurationCache("")
}
tasks.withType(Sign::class.java) {
  notCompatibleWithConfigurationCache("")
  // skip signing for -SNAPSHOT publishing
  onlyIf { !VERSION_CURRENT.endsWith("SNAPSHOT") }
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

tasks.withType(AbstractDokkaLeafTask::class.java) {

  // Dokka doesn't support configuration caching
  notCompatibleWithConfigurationCache("Dokka doesn't support configuration caching")

  // Dokka uses their outputs but doesn't explicitly depend upon them.
  mustRunAfter(tasks.withType(KotlinCompile::class.java))
  mustRunAfter(tasks.withType(LintTask::class.java))
  mustRunAfter(tasks.withType(FormatTask::class.java))

  moduleName.set("ktrules")

  dokkaSourceSets.getByName("main") {

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
      localDirectory.set(file("src/main"))

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

tasks.withType<SpotlessTask> {
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
      "kotlin block in markdown",
      //language=regexp
      """\R```kotlin.*\n((?:(?! *```)[\s\S])*)""",
      KotlinExtension::class.java
    ) {

      // KtLint 0.48.0+ is failing when trying to format the snippets
      ktlint("0.47.1")
        .setUseExperimental(true)
        .setEditorConfigPath(file(".editorconfig"))
        // Editorconfig doesn't work for code blocks, since they don't have a path which matches the
        // globs.  The band-aid is to parse kotlin settings out the .editorconfig, then pass all the
        // properties in as a map.
        .editorConfigOverride(editorConfigKotlinProperties())
    }

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

fun editorConfigKotlinProperties(): Map<String, String> {

  val ecFile = file(".editorconfig")

  return extensions.extraProperties.getOrPut(ecFile.path) {
    editorConfigKotlinProperties(ecFile, rootDir)
  }
}

inline fun <reified T> ExtraPropertiesExtension.getOrPut(name: String, default: () -> T): T {
  return getOrNullAs<T>(name) ?: default().also { set(name, it) }
}

fun ExtraPropertiesExtension.getOrNull(name: String): Any? = if (has(name)) get(name) else null

inline fun <reified T> ExtraPropertiesExtension.getOrNullAs(name: String): T? {
  val existing = getOrNull(name) ?: return null
  return existing as T
}

fun editorConfigKotlinProperties(
  editorConfigFile: File,
  rootDir: File
): Map<String, String> {

  val myCache = Caches.none()
  val propService = ResourcePropertiesService.builder()
    .cache(myCache)
    .defaultEditorConfig(editorConfig(editorConfigFile))
    .rootDirectory(rootDir.toResourcePaths())
    .build()

  return propService
    .queryProperties(rootDir.resolve("foo.kt").toResource())
    .properties
    .values
    .associate { it.name to it.sourceValue }
}

fun editorConfig(editorConfigFile: File): EditorConfig {

  val parser = EditorConfigParser.builder().build()
  val resource = editorConfigFile.toResource()

  val handler = EditorConfigModelHandler(PropertyTypeRegistry.default_(), Version.CURRENT)

  parser.parse(resource, handler, ErrorHandler.THROW_SYNTAX_ERRORS_IGNORE_OTHERS)

  return handler.editorConfig
}

fun File.toResource() = Resources.ofPath(toPath(), StandardCharsets.UTF_8)
fun File.toResourcePaths() = ResourcePaths.ofPath(toPath(), StandardCharsets.UTF_8)

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

  tagName { versionString }
  releaseName { versionString }

  generateReleaseNotes.set(false)

  body {

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
    val versionSectionRegex = """$currentVersionRegex\n([\s\S]*?)(?=\n+$lastVersionRegex)""".toRegex()

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

  overwrite.set(false)
  dryRun.set(false)
  draft.set(true)
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
