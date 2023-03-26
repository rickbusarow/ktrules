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

import com.diffplug.gradle.spotless.GroovyGradleExtension
import com.diffplug.gradle.spotless.KotlinExtension
import com.diffplug.gradle.spotless.SpotlessTask
import com.github.javaparser.printer.concretesyntaxmodel.CsmElement.token
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.rickbusarow.docusync.gradle.DocusyncTask
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
import org.gradle.internal.impldep.org.bouncycastle.asn1.x500.style.RFC4519Style.owner
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Properties as JavaProperties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.docusync)
  alias(libs.plugins.dokka)
  alias(libs.plugins.github.release)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.kotlinx.binaryCompatibility)
  alias(libs.plugins.shadowJar)
  alias(libs.plugins.spotless)
  alias(libs.plugins.vanniktech.publish)
}

dependencies {

  compileOnly(libs.google.auto.service.annotations)
  compileOnly(libs.jetbrains.markdown)
  compileOnly(libs.kotlin.compiler)
  compileOnly(libs.kotlin.reflect)
  compileOnly(libs.ktlint.core)
  compileOnly(libs.ktlint.ruleset.standard)
  compileOnly(libs.slf4j.api)

  detektPlugins(libs.detekt.rules.libraries)

  ksp(libs.zacSweers.auto.service.ksp)

  testImplementation(libs.google.auto.service.annotations)
  testImplementation(libs.jetbrains.markdown)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.common)
  testImplementation(libs.kotest.extensions)
  testImplementation(libs.kotest.property.jvm)
  testImplementation(libs.kotlin.compiler)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.ktlint.core)
  testImplementation(libs.ktlint.ruleset.standard)
  testImplementation(libs.ktlint.test)
  testImplementation(libs.slf4j.api)
}

docusync {
  docSet {
    docs("README.md")
    // docs(fileTree(projectDir) {
    //   include("**/*.md", "**/*.mdx")
    // })
    sampleCodeSource.from(fileTree(projectDir.resolve("src/test/kotlin")) {
      include("**/*.kt")
    })
    rule("maven-artifact") {
      regex = maven(group = "com\\.rickbusarow\\.ktrules")
      replacement = "$1:$2:$version"
    }

    rule("editorconfig-sample") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.ktrules.KtRulesRuleSetProviderTest.Environment.defaultConfig",
        bodyOnly = true,
        codeBlockLanguage = "editorconfig"
      )
    }
  }
}

tasks.withType<DocusyncTask> { mustRunAfter(tasks.withType<KotlinCompile>()) }

tasks.named("apiCheck") { mustRunAfter("apiDump") }

// dummy ktlint-gradle plugin task names which just delegate to the Kotlinter ones
val ktlintCheck by tasks.registering { dependsOn("lintKotlin") }
val ktlintFormat by tasks.registering { dependsOn("formatKotlin") }

tasks.named("lintKotlin") { mustRunAfter("formatKotlin") }
tasks.withType<ConfigurableKtLintTask> {
  source(buildFile)
  if (project == project.rootProject) {
    source(file("settings.gradle.kts"))
  }
  System.getProperties()
    .setIfAbsent("ktrules.current_version") { libs.versions.ktrules.get() }
}

fun JavaProperties.setIfAbsent(name: String, value: () -> String) {
  if (getProperty(name) == null) {
    setProperty(name, value())
  }
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

version = libs.versions.ktrules.get()

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

  tasks.named("compileTestKotlin", KotlinCompile::class) {
    kotlinOptions {

      check(libs.versions.kotest.get() == "5.5.5") { "Remove the KotestInternal compiler arg" }

      freeCompilerArgs += "-opt-in=io.kotest.common.KotestInternal"
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      allWarningsAsErrors = false

      val kotlinMajor = libs.versions.kotlinApi.get()
      languageVersion = kotlinMajor
      apiVersion = kotlinMajor

      jvmTarget = libs.versions.jvmTarget.get()
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
  dependsOn("docusync")
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
    groupId = "com.rickbusarow.ktrules",
    artifactId = "ktrules",
    version = libs.versions.ktrules.get()
  )

  publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)

  signAllPublications()

  pom {
    description.set("ktlint rules")
    name.set("ktrules")

    url.set("https://www.github.com/rbusarow/ktrules/")

    licenses {
      license {
        name.set("The Apache Software License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("repo")
      }
    }
    scm {
      url.set("https://www.github.com/rbusarow/ktrules/")
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
  onlyIf { !(version as String).endsWith("SNAPSHOT") }
}

val checkVersionIsSnapshot by tasks.registering {
  group = "publishing"
  description = "ensures that the project version has a -SNAPSHOT suffix"
  val versionString = version as String
  doLast {
    val expected = "-SNAPSHOT"
    require(versionString.endsWith(expected)) {
      "The project's version name must be suffixed with `$expected` when checked in" +
        " to the main branch, but instead it's `$versionString`."
    }
  }
}

val checkVersionIsNotSnapshot by tasks.registering {
  group = "publishing"
  description = "ensures that the project version does not have a -SNAPSHOT suffix"
  val versionString = version as String
  doLast {
    require(!versionString.endsWith("-SNAPSHOT")) {
      "The project's version name cannot have a -SNAPSHOT suffix, but it was $versionString."
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

    val readmeFile = file("README.md")

    if (readmeFile.exists()) {
      includes.from(readmeFile)
    }

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

val classifier = if (plugins.hasPlugin("java-gradle-plugin")) "" else "all"

configurations.named("compileOnly") { extendsFrom(project.configurations.getByName("shadow")) }

val shadowJar = tasks.named("shadowJar", ShadowJar::class.java) {

  configurations = listOf(project.configurations.getByName("shadow"))

  listOf(
    "org.intellij.markdown",
    "org.snakeyaml",
  ).forEach {
    relocate(it, "com.rickbusarow.ktrules.$it")
  }

  archiveClassifier.convention(classifier)
  archiveClassifier.set(classifier)

  transformers.add(ServiceFileTransformer())

  minimize()

  exclude("**/*.kotlin_metadata")
  exclude("**/*.kotlin_module")
  exclude("META-INF/maven/**")
}

// By adding the task's output to archives, it's automatically picked up by Gradle's maven-publish
// plugin and added as an artifact to the publication.
artifacts {
  add("runtimeOnly", shadowJar)
  add("archives", shadowJar)
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

/** Parses an .editorconfig file at [editorConfigFile]. */
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

  val versionString = version.toString()

  token {
    property("GITHUB_PERSONAL_ACCESS_TOKEN") as? String
      ?: throw GradleException(
        "In order to release, you must provide a GitHub Personal Access Token " +
          "as a property named 'GITHUB_PAT'.  " +
          "See https://squareup.github.io/kable/docs/next/contributing/items/releasing"
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
