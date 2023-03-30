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

package com.rickbusarow.ktrules.rules.internal

import java.util.Locale

/**
 * Replaces the deprecated Kotlin version, but hard-codes `Locale.US`
 *
 * @since 1.0.1
 */
internal fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

/**
 * Removes trailing whitespaces from all lines in a string.
 *
 * Shorthand for `lines().joinToString("\n") { it.trimEnd() }`
 *
 * @since 1.0.1
 */
internal fun String.trimLineEnds(): String = mapLines { it.trimEnd() }

/**
 * performs [transform] on each line
 *
 * @since 1.0.1
 */
internal fun String.mapLines(
  transform: (String) -> CharSequence
): String = lineSequence()
  .joinToString("\n", transform = transform)

internal fun String.prefixIfNot(prefix: String) =
  if (this.startsWith(prefix)) this else "$prefix$this"

/**
 * shorthand for `replace(___, "")` against multiple tokens
 *
 * @since 1.0.1
 */
internal fun String.remove(vararg strings: String): String = strings.fold(this) { acc, string ->
  acc.replace(string, "")
}

/**
 * shorthand for `replace(___, "")` against multiple tokens
 *
 * @since 1.0.1
 */
internal fun String.remove(vararg regex: Regex): String = regex.fold(this) { acc, reg ->
  acc.replace(reg, "")
}
