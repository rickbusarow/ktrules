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

package com.rickbusarow.ktrules.rules.wrapping

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
