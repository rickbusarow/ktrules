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

import builds.capitalize
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.DependencyNotationSupplier
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("module")
  idea
}

jvmModule {
  poko()
  published(
    artifactId = "ktrules-api",
    pomDescription = "KtLint rules"
  )
}

val test: SourceSet by sourceSets.getting
val main: SourceSet by sourceSets.getting

val kotlinMain: KotlinSourceSet by kotlin.sourceSets.named("main")
val kotlinMainTest: KotlinSourceSet by kotlin.sourceSets.named("test")

val compilations = kotlin.target.compilations

val kotlinMainCompilation by compilations.named("main")
val kotlinMainTestCompilation by compilations.named("test")

val testAll = tasks.register("testAll", Test::class) {
  group = "Verification"
}

val compatSourceSetNames = listOf(
  Compat("compat47") {
    api(libs.ktlint47.ruleset.standard)
    api(libs.ktlint47.core)
    testImplementation(libs.ktlint47.test)
  },
  Compat("compat48") {
    api(libs.ktlint48.core)
    testImplementation(libs.ktlint48.test)
  },
  Compat("compat49") {
    api(libs.ktlint49.cli.ruleset.core)
    api(libs.ktlint49.rule.engine.core)
    testImplementation(libs.ktlint49.cli.ruleset.core)
    testImplementation(libs.ktlint49.rule.engine)
    testImplementation(libs.ktlint49.rule.engine.core)
    testImplementation(libs.ktlint49.test)
  },
  Compat("compat50") {
    api(libs.ktlint50.cli.ruleset.core)
    api(libs.ktlint50.rule.engine.core)
    testImplementation(libs.ktlint50.cli.ruleset.core)
    testImplementation(libs.ktlint50.rule.engine)
    testImplementation(libs.ktlint50.rule.engine.core)
    testImplementation(libs.ktlint50.test)
  },
  Compat("compat100") {
    api(libs.ktlint100.cli.ruleset.core)
    api(libs.ktlint100.rule.engine.core)
    testImplementation(libs.ktlint100.cli.ruleset.core)
    testImplementation(libs.ktlint100.rule.engine)
    testImplementation(libs.ktlint100.rule.engine.core)
    testImplementation(libs.ktlint100.test)
  },
  Compat("compat110") {
    api(libs.ktlint110.cli.ruleset.core)
    api(libs.ktlint110.rule.engine.core)
    testImplementation(libs.ktlint110.cli.ruleset.core)
    testImplementation(libs.ktlint110.rule.engine)
    testImplementation(libs.ktlint110.rule.engine.core)
    testImplementation(libs.ktlint110.test)
  }
)

val compats = compatSourceSetNames
  .asSequence()
  .onEach(Compat::writeSrcDirs)
  .onEach { compat ->
    java {
      registerFeature(compat.ssName) {
        usingSourceSet(compat.ss)
        for (capability in compat.capabilities.map { it.get() }) {
          capability(capability.group, capability.name, capability.version)
        }
      }
    }
  }
  .onEach { compat ->
    tasks.named("check") {
      dependsOn(compat.testTaskProvider)
    }
    testAll {
      dependsOn(compat.testTaskProvider)
    }
  }
  .onEach { compat ->
    idea {
      module {
        testSources.from(provider { compat.testSS.allSource.srcDirs })
      }
    }
  }
  .toList()

val compileCompat by tasks.registering {
  dependsOn(compats.map(Compat::compileKotlinTask))
}
val compileCompatTest by tasks.registering {
  dependsOn(compats.map(Compat::compileTestKotlinTask))
}
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

  compats.forEach { it.testImplementationConfig.dependencies.addLater(libs.rickBusarow.kase) }
}

class Compat(
  val ssName: String,
  dependenciesBuilder: CompatDependencyScope.() -> Unit
) {

  private val testSSName = "${ssName}Test"
  private val testTaskName = "test${testSSName.capitalize()}"
  private val jarTaskName = "${ssName}Jar"
  private val sourcesJarTaskName = "${ssName}SourcesJar"

  val ss: SourceSet by java.sourceSets.register(ssName) ss@{
    java.srcDir("src/$ssName/kotlin")
    resources.srcDir("src/$ssName/resources")

    compileClasspath += main.output
    runtimeClasspath += main.output
  }
  val kss: NamedDomainObjectProvider<KotlinSourceSet> = kotlinExtension.sourceSets
    .named(ssName) kss@{
      this@kss.dependsOn(kotlinMain)
    }

  val testSS by sourceSets.register(testSSName) ss@{
    this@ss.java.srcDir("src/$testSSName/kotlin")
    this@ss.resources.srcDir("src/$testSSName/resources")
    this@ss.compileClasspath += (ss.output + test.output + main.compileClasspath)
    this@ss.runtimeClasspath += (ss.output + test.output)
  }

  val kotlinTestSS: KotlinSourceSet by kotlinExtension.sourceSets.named(testSSName) kss@{
    this@kss.dependsOn(kotlinMainTest)
  }

  val jarTaskProvider = tasks.register(jarTaskName, Jar::class) {
    from(ss.output)
    from(main.output)
    archiveFileName.set("$ssName.jar")
  }
  val sourcesJarTaskProvider = tasks.register(sourcesJarTaskName, Jar::class) {
    archiveClassifier.set("sources")
    from(ss.allSource)
    from(main.allSource)
  }

  val testTaskProvider = tasks.register(testTaskName, Test::class) {
    description = "Runs tests for the $ssName source set."
    group = "Verification"

    testClassesDirs = testSS.output.classesDirs
    classpath = testSS.runtimeClasspath
  }

  val compileKotlinTaskName = "compile${ssName.capitalize()}Kotlin"
  val compileTestKotlinTaskName = "compile${testSSName.capitalize()}Kotlin"

  val compileKotlinTask: KotlinCompile
    by tasks.named(compileKotlinTaskName, KotlinCompile::class)
  val compileTestKotlinTask: KotlinCompile
    by tasks.named(compileTestKotlinTaskName, KotlinCompile::class)

  val testCompilation: KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>
    by compilations.named(testSSName) compilation@{

      this@compilation.associateWith(kotlinMainCompilation)
    }

  val apiConfig by configurations.named(ss.apiConfigurationName)
  val implementationConfig by configurations.named(ss.implementationConfigurationName)
  val testImplementationConfig by configurations.named(testSS.implementationConfigurationName)
  val compileOnlyConfig by configurations.named(ss.compileOnlyConfigurationName)

  val capabilities: List<Provider<CompatCapability>> = CompatDependencyScope(
    apiConfig = apiConfig,
    implementationConfig = implementationConfig,
    testImplementationConfig = testImplementationConfig,
    compileOnlyConfig = compileOnlyConfig
  ).apply(dependenciesBuilder)
    .capabilities

  fun writeSrcDirs() {
    file("src/$ssName/kotlin/com/rickbusarow/ktrules").mkdirs()
    file("src/$testSSName/kotlin/com/rickbusarow/ktrules").mkdirs()
  }
}

/**
 * Just a Maven coordinate broken down into three segments.
 *
 * @property group ex: `com.pinterest.ktlint`
 * @property name ex: `ktlint-core`
 * @property version ex: `0.47.1`
 */
data class CompatCapability(val group: String, val name: String, val version: String)

class CompatDependencyScope(
  private val apiConfig: Configuration,
  private val implementationConfig: Configuration,
  private val testImplementationConfig: Configuration,
  private val compileOnlyConfig: Configuration
) {

  val capabilities = mutableListOf<Provider<CompatCapability>>()
  private fun capability(notation: Provider<MinimalExternalModuleDependency>) {
    capabilities.add(
      notation.map {
        CompatCapability(
          group = it.group!!,
          name = it.name,
          version = it.version!!
        )
      }
    )
  }

  fun api(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    apiConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  fun compileOnly(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    compileOnlyConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  fun implementation(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    implementationConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  fun testImplementation(dependencyNotation: DependencyNotationSupplier) {
    testImplementation(dependencyNotation.asProvider())
  }

  fun testImplementation(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    testImplementationConfig.dependencies.addLater(dependencyNotation)
  }
}
