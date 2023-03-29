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

      val regex = buildString {
        // match any of the following
        append("(?:")

        // match anything inside complex markdown links like `[Some Text](some link)`
        append("\\[[^\\]]*]\\([^)]*\\)")

        append('|')

        // match anything inside markdown links like `[Some Text]`
        append("\\[[^\\]]*]")

        append('|')

        // match anything inside backticks
        append("`[^`]*`")

        append('|')

        // match anything inside single underscores or asterisks
        append("[*_][^*_]*[*_]")

        append('|')

        // match anything inside double underscores or asterisks
        append("[*_]{2}[^*_]*[*_]{2}")

        append('|')

        // match anything inside inline code blocks
        append("`{3}[^`]*`{3}")

        append('|')

        // match anything that's not a space or a markdown delimiter
        append("[^\\s*_`~\\[\\]]+")

        // close the group and ensure that anything following is included
        append(")\\S?")
      }.toRegex()

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
