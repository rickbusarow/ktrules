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

package com.rickbusarow.ktrules.rules

internal fun interface StringWrapper {
  fun wrap(
    words: List<String>,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ): String

  companion object {

    internal fun String.splitWords(): List<String> {

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
        // match anything inside bold text using underscores like __really mean it__
        append("""__[^\s](?:(?!__).)*[^\s]__""")
        append('|')
        // match anything inside italicized text using underscores like _actually_
        append("""_[^\s_](?:(?!_).)*[^\s_]_""")
        append('|')
        // match anything inside bold text using underscores like **really mean it**
        append("""\*\*[^\s](?:(?!\*\*).)*[^\s]\*\*""")
        append('|')
        // match anything inside italicized text using asterisks like _actually_
        append("""\*[^\s\*](?:(?!\*).)*[^\s\*]\*""")

        append('|')
        // match anything that's not a whitespace
        //language=regexp
        append("""\S+""")

        // close the group
        append(")")

        // allow for repeats of the above patterns so long as there are no white spaces between them
        append("+")
      }.toRegex(RegexOption.DOT_MATCHES_ALL)

      return regex.findAll(this).map { it.value }.toList()
    }
  }
}

internal class GreedyWrapper : StringWrapper {
  override fun wrap(
    words: List<String>,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ): String {
    val paragraph = StringBuilder()
    var currentLine = StringBuilder(leadingIndent)
    var isFirstWord = true

    for (word in words) {
      val currentWordLength = word.length
      val currentLineLength = currentLine.length

      when {
        isFirstWord -> {
          currentLine.append(word)
          isFirstWord = false
        }

        currentLineLength + 1 + currentWordLength <= maxLength -> {
          // The word fits on the current line with a space before it.
          currentLine.append(" ").append(word)
        }

        else -> {
          // The word does not fit on the current line, so start a new line.
          paragraph.append(currentLine).append("\n")
          currentLine = StringBuilder(continuationIndent).append(word)
        }
      }
    }

    // Add the last line to the wrapped text.
    paragraph.append(currentLine)

    return paragraph.toString()
  }
}

internal class MinimumRaggednessWrapper : StringWrapper {
  override fun wrap(
    words: List<String>,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ): String {

    val wordCount = words.size

    val minCosts = IntArray(wordCount + 1) { Int.MAX_VALUE }
    val splitIndices = IntArray(wordCount + 1)

    minCosts[wordCount] = 0

    val reversedIndices = wordCount - 1 downTo 0

    // Iterate through words in reverse order
    for (currentWord in reversedIndices) {
      var lineLength = if (currentWord == 0) leadingIndent.length else continuationIndent.length
      var nextWord = currentWord
      // Calculate line lengths and costs
      while (nextWord < wordCount) {
        lineLength += words[nextWord].length + 1
        if (lineLength - 1 > maxLength && nextWord > currentWord) break

        val cost = minCosts[nextWord + 1] + (maxLength - lineLength + 1).let { it * it }
        if (cost < minCosts[currentWord]) {
          minCosts[currentWord] = cost
          splitIndices[currentWord] = nextWord + 1
        }
        nextWord++
      }
    }

    // Build the wrapped string using the splitIndices
    return buildString {
      var currentWordIndex = 0
      while (currentWordIndex < wordCount) {
        val nextSplit = splitIndices[currentWordIndex]
        if (currentWordIndex == 0) {
          append(leadingIndent)
        } else {
          append(continuationIndent)
        }

        val lineIndices = currentWordIndex until nextSplit

        for (wordIndex in lineIndices) {
          append(words[wordIndex])
          if (wordIndex < nextSplit - 1) {
            append(' ')
          }
        }
        if (nextSplit < wordCount) {
          append('\n')
        }
        currentWordIndex = nextSplit
      }
    }
  }
}
