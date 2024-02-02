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

package builds

import com.rickbusarow.kgx.javaExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.Serializable

interface KotlinJvmExtension : KotlinExtension
interface KotlinMultiplatformExtension : KotlinExtension
interface KotlinExtension : Serializable {

  val allWarningsAsErrors: Property<Boolean>
  val explicitApi: Property<Boolean>
}

abstract class BaseKotlinConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val extension = target.extensions.getByType(KotlinExtension::class.java)

    val jetbrainsExtension = target.kotlinExtension
    jetbrainsExtension.jvmToolchain(target.JDK_INT)

    configureKotlinOptions(target, extension)

    target.plugins.withId("java") {
      target.tasks.withType(JavaCompile::class.java).configureEach { task ->
        task.options.release.set(target.JVM_TARGET_INT)
      }

      target.javaExtension.sourceCompatibility = JavaVersion.toVersion(target.JVM_TARGET)
    }
  }

  private fun configureKotlinOptions(target: Project, extension: KotlinExtension) {
    target.tasks.withType(KotlinCompile::class.java).configureEach { task ->
      task.kotlinOptions {

        options.allWarningsAsErrors.set(extension.allWarningsAsErrors.orElse(false))

        val kotlinMajor = target.KOTLIN_API
        languageVersion = kotlinMajor
        apiVersion = kotlinMajor

        jvmTarget = target.JVM_TARGET

        @Suppress("SpellCheckingInspection")
        freeCompilerArgs += buildList {
          add("-Xinline-classes")
          add("-Xcontext-receivers")

          val sourceSetName = (task as? BaseKotlinCompile)?.sourceSetName?.orNull

          val shouldBeStrict = when {
            extension.explicitApi.orNull == false -> false
            sourceSetName == null -> false
            sourceSetName.endsWith("test", true) -> false
            else -> true
          }
          if (shouldBeStrict) {
            add("-Xexplicit-api=strict")
          }
        }
      }
    }
  }
}
