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

import org.intellij.lang.annotations.Language
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
 * Doesn't preserve the original line endings.
 *
 * @since 1.0.1
 */
internal fun CharSequence.mapLines(
  transform: (String) -> CharSequence
): String = lineSequence()
  .joinToString("\n", transform = transform)

/**
 * performs [transform] on each line
 *
 * Doesn't preserve the original line endings.
 *
 * @since 1.0.4
 */
internal fun CharSequence.mapLinesIndexed(
  transform: (Int, String) -> CharSequence
): String = buildString {
  this@mapLinesIndexed
    .lineSequence()
    .forEachIndexed { i, line ->
      appendLine(transform(i, line))
    }
}

/**
 * Prepends [continuationIndent] to every line of the original string.
 *
 * Doesn't preserve the original line endings.
 *
 * @since 1.0.4
 */
internal fun CharSequence.prependContinuationIndent(
  continuationIndent: String
): String = mapLinesIndexed { i, line ->
  when {
    i == 0 -> line
    line.isBlank() -> line
    else -> "$continuationIndent$line"
  }
}

/**
 * `"$prefix$this$suffix"`
 *
 * @since 1.0.4
 */
internal fun CharSequence.wrapIn(
  prefix: String,
  suffix: String = prefix
): String = "$prefix$this$suffix"

internal fun CharSequence.prefix(prefix: String): String = "$prefix$this"

internal fun String.prefixIfNot(prefix: String): String {
  return if (this.startsWith(prefix)) this else "$prefix$this"
}

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

/**
 * shorthand for `replace(___, "")` against multiple tokens. The input strings are converted to
 * Regex before replacement.
 */
internal fun String.removeRegex(
  @Language("regexp") vararg regex: String
): String = regex.fold(this) { acc, reg ->
  acc.replace(reg.toRegex(), "")
}

/**
 * replace ` ` with `·`
 *
 * @since 1.0.4
 */
internal val String.dots get() = replace(" ", "·")

/**
 * replace `·` with ` `
 *
 * @since 1.0.4
 */
internal val String.noDots get() = replace("·", " ")

/**
 * Adds line breaks and indents to the output of data class `toString()`s.
 *
 * @see toStringPretty
 * @since 1.0.5
 */
internal fun String.prettyToString(): String {
  return replace(",", ",\n")
    .replace("(", "(\n")
    .replace(")", "\n)")
    .replace("[", "[\n")
    .replace("]", "\n]")
    .replace("{", "{\n")
    .replace("}", "\n}")
    .replace("\\(\\s*\\)".toRegex(), "()")
    .replace("\\[\\s*]".toRegex(), "[]")
    .indentByBrackets()
    .replace("""\n *\n""".toRegex(), "\n")
}

/**
 * shorthand for `toString().prettyToString()`, which adds line breaks and indents to a string
 *
 * @see prettyToString
 * @since 1.0.5
 */
internal fun Any?.toStringPretty(): String = when (this) {
  is Map<*, *> -> toList().joinToString("\n")
  else -> toString().prettyToString()
}

/**
 * A naive auto-indent which just counts brackets.
 *
 * @since 1.0.5
 */
internal fun String.indentByBrackets(tab: String = "  "): String {

  var tabCount = 0

  val open = setOf('{', '(', '[', '<')
  val close = setOf('}', ')', ']', '>')

  return lines()
    .map { it.trim() }
    .joinToString("\n") { line ->

      if (line.firstOrNull() in close) {
        tabCount--
      }

      "${tab.repeat(tabCount)}$line"
        .also {

          // Arrows aren't brackets
          val noSpecials = line.remove("<=", "->")

          tabCount += noSpecials.count { char -> char in open }
          // Skip the first char because if it's a closing bracket, it was already counted above.
          tabCount -= noSpecials.drop(1).count { char -> char in close }
        }
    }
}
