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

import com.rickbusarow.ktrules.rules.Tests
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper.Companion.splitWords
import org.junit.jupiter.api.Test

internal class GreedyWrapperTest : Tests {

  @Test
  fun `a long paragraph is wrapped`() {

    wrap(
      "This is a very long sentence with several words that need to be wrapped.",
      maxLength = 30,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |This is a very long sentence
      |with several words that need
      |to be wrapped.
    """.trimMargin()
  }

  @Test
  fun `wrap with leading and continuation indent`() {

    wrap(
      "This is a test with leading and continuation indent applied correctly.",
      maxLength = 20,
      leadingIndent = "   ",
      continuationIndent = "      "
    ) shouldBe """
      |   This is a test
      |      with leading
      |      and
      |      continuation
      |      indent applied
      |      correctly.
    """.trimMargin()
  }

  @Test
  fun `wrap with special characters`() {

    wrap(
      "`val thisIsInlineCodeWhichShouldNotWrap : String` and [this is a long link text](https://example.com).",
      maxLength = 25,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |`val thisIsInlineCodeWhichShouldNotWrap : String`
      |and
      |[this is a long link text](https://example.com).
    """.trimMargin()
  }

  @Test
  fun `wrap very short lines`() {

    wrap(
      "This is a test sentence.",
      maxLength = 5,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |This
      |is a
      |test
      |sentence.
    """.trimMargin()
  }

  @Test
  fun `wrap with long word at the start`() {

    wrap(
      "ThisIsAVeryVeryVeryLongWord that does not fit the line length.",
      maxLength = 20,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |ThisIsAVeryVeryVeryLongWord
      |that does not fit
      |the line length.
    """.trimMargin()
  }

  @Test
  fun `wrap with long word in the middle`() {

    wrap(
      "This is a ThisIsAVeryVeryVeryLongWord that does not fit the line length.",
      maxLength = 20,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |This is a
      |ThisIsAVeryVeryVeryLongWord
      |that does not fit
      |the line length.
    """.trimMargin()
  }

  @Test
  fun `wrap with long word at the end`() {

    wrap(
      "This is a test sentence with ThisIsAVeryVeryVeryLongWord.",
      maxLength = 20,
      leadingIndent = "",
      continuationIndent = ""
    ) shouldBe """
      |This is a test
      |sentence with
      |ThisIsAVeryVeryVeryLongWord.
    """.trimMargin()
  }

  private fun wrap(
    input: String,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ) = GreedyWrapper()
    .wrap(
      words = input.replace(' ', ' ').splitWords(),
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    )
    .replace(' ', ' ')
}
