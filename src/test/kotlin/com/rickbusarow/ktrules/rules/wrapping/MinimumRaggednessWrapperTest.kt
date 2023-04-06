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

import com.rickbusarow.ktrules.rules.Tests
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper.Companion.splitWords
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MinimumRaggednessWrapperTest : Tests {

  @Test
  fun `should wrap text with single space`() {
    val input = "This is a test."
    val maxLength = 10
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = """
      |  This
      |    is a
      |    test.
    """.trimMargin()
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `should wrap text with multiple spaces`() {
    val input = "This  is   a    test."
    val maxLength = 10
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = """
      |  This
      |    is a
      |    test.
    """.trimMargin()
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `should handle text with backticks`() {
    val input = "This is `a test with` backticks."
    val maxLength = 10
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = """
      |  This is
      |    `a test with`
      |    backticks.
    """.trimMargin()
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `should handle single word exceeding maxLength`() {
    val input = "This is a veryverylongwordthatexceedsthemaxlength."
    val maxLength = 10
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = """
      |  This
      |    is a
      |    veryverylongwordthatexceedsthemaxlength.
    """.trimMargin()
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `should handle empty input`() {
    val input = ""
    val maxLength = 10
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = ""
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `should handle input shorter than maxLength`() {
    val input = "This is a short test."
    val maxLength = 25
    val leadingIndent = "  "
    val continuationIndent = "    "
    val expectedOutput = "  This is a short test."
    wrap(
      input = input,
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    ) shouldBe expectedOutput
  }

  @Test
  fun `minimum raggedness output has no more lines than the greedy version`() {
    val input =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur ullamcorper sapien " +
        "vitae mi auctor, et sollicitudin nibh condimentum. " +
        "Etiam elementum ligula a lectus posuere, id blandit nulla faucibus."
    val maxLength = 50
    val leadingIndent = ""
    val continuationIndent = ""

    val greedyWrapper = GreedyWrapper()
    val minimumRaggednessWrapper = MinimumRaggednessWrapper()

    val greedyWrapped = greedyWrapper.wrap(
      words = input.splitWords(),
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    )

    val minimumRaggednessWrapped = minimumRaggednessWrapper.wrap(
      words = input.splitWords(),
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    )

    val greedyWrappedLines = greedyWrapped.lines()
    val minimumRaggednessWrappedLines = minimumRaggednessWrapped.lines()

    minimumRaggednessWrappedLines.size shouldBe greedyWrappedLines.size
  }

  private fun wrap(
    input: String,
    maxLength: Int,
    leadingIndent: String,
    continuationIndent: String
  ) = MinimumRaggednessWrapper()
    .wrap(
      words = input.replace(' ', ' ').splitWords(),
      maxLength = maxLength,
      leadingIndent = leadingIndent,
      continuationIndent = continuationIndent
    )
    .replace(' ', ' ')
}
