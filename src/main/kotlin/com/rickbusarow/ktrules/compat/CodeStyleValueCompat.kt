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

package com.rickbusarow.ktrules.compat

/**
 * Code style to be used while linting and formatting. Note that the [EnumValueParser] requires
 * values to be lowercase.
 *
 * @since 1.1.1
 */
@Suppress("EnumEntryName", "EnumNaming")
enum class CodeStyleValueCompat {
  @Deprecated(
    message = "Marked for removal in KtLint 0.50. Value is renamed to 'android_studio'.",
    replaceWith = ReplaceWith("android_studio")
  )
  android,

  /**
   * Code formatting based on Android's Kotlin styleguide
   * (https://developer.android.com/kotlin/style-guide). This code style aims to be compatible with
   * code formatting in Android Studio.
   *
   * @since 1.1.1
   */
  android_studio,

  /**
   * Code formatting based on Kotlin Coding conventions
   * (https://kotlinlang.org/docs/coding-conventions.html). This code style aims to be compatible
   * with code formatting in IntelliJ IDEA.
   *
   * @since 1.1.1
   */
  intellij_idea,

  /**
   * Code formatting based on the best of both the Kotlin Coding conventions
   * (https://kotlinlang.org/docs/coding-conventions.html) and Android's Kotlin styleguide
   * (https://developer.android.com/kotlin/style-guide). This codestyle also provides additional
   * formatting on topics which are not (explicitly) mentioned in the before mentioned styleguide.
   * Also, this code style sometimes formats code in a way which is not compatible with the default
   * code formatters in IntelliJ IDEA and Android Studio. When using this codestyle, it is best to
   * disable (e.g. not use) automatic code formatting in the editor. Mean reason for deviating from
   * the code formatting provided by the editor is that those contain bugs which after some years
   * are still not fixed. In the long run, this code style becomes the default code style provided
   * by KtLint.
   *
   * @since 1.1.1
   */
  ktlint_official,

  @Deprecated(
    message = "Marked for removal in KtLint 0.50. Value is renamed to 'intellij_idea'.",
    replaceWith = ReplaceWith("intellij_idea")
  )
  official
}
