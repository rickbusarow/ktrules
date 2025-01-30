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

package com.rickbusarow.ktrules.rules.wrapping

import com.rickbusarow.ktrules.rules.internal.letIf
import kotlin.LazyThreadSafetyMode.NONE

internal fun interface StringWrapper {
  fun wrap(
    words: List<String>,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ): String

  companion object {

    internal fun String.splitWords(
      preserveLeadingWhitespace: Boolean = true,
      preserveTrailingWhitespace: Boolean = false
    ): List<String> {

      val wrappedInParenthesis = """\((?:[^()\\]+|\\.|\((?:[^()\\]+|\\.)*\))*\)"""
      val wrappedInSquareBrackets = """\[(?:[^\]\[]|\[.*?\])*\]"""

      val regex = buildString {
        // non-capturing group which matches any of the following patterns separated by a '|'
        // The order of these patterns matters, since the matching is done greedily.
        append("(?:")

        // match anything inside inline-style Markdown links like `[Some Text](some link)`
        append("$wrappedInSquareBrackets$wrappedInParenthesis")
        append('|')
        // match anything inside shortcut-style Markdown links like `[Some Text][some link]`
        append("$wrappedInSquareBrackets$wrappedInSquareBrackets")
        append('|')
        // match anything inside simple reference-style Markdown links like [Some Text]
        append(wrappedInSquareBrackets)
        append('|')
        // match anything inside inline code blocks with three backticks like ```fun foo() = Unit```
        append("""```[^`]*(?:`[^`]+`[^`]*)*+```""")
        append('|')
        // match anything inside inline code with a single backtick like `fun foo() = Unit`
        append("`(?:(?!`).)*`")

        append('|')
        // Match [*_~] which are used as wrappers like _italicized_ or **bold**.
        //
        // ([*_~])(\1*)        captures the very first character in $1,
        //                     then captures any repetitions in $2
        // (?!\1|\s)[^\n]+?\S  matches anything except a newline,
        //                     as long as the entire substring starts and ends with a non-whitespace
        // \1\2\S*             matches the same delimiter sequence as the beginning,
        //                     followed by any additional non-whitespace characters
        append("""([*_~])(\1*)(?!\1|\s)[^\n]+?\S\1\2\S?""")

        append('|')
        // match anything that's not a whitespace
        //language=regexp
        append("""\S+""")

        // close the group
        append(")")

        // allow for repeats of the above patterns so long as there are no white spaces between them
        append("+")
      }.toRegex()

      val original: String = this@splitWords
      val leadingWhitespace by lazy(NONE) { original.takeWhile { it == ' ' } }
      val trailingWhitespace by lazy(NONE) { original.takeLastWhile { it == ' ' } }

      return regex.findAll(this)
        .toList()
        .let { matches ->

          matches
            .mapIndexed { index, matchResult ->

              matchResult.value
                .letIf(index == 0 && preserveLeadingWhitespace) {
                  leadingWhitespace + this@letIf
                }
                .letIf(index == matches.lastIndex && preserveTrailingWhitespace) {
                  this@letIf + trailingWhitespace
                }
            }
        }
    }
  }
}
