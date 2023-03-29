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

import com.rickbusarow.ktrules.rules.StringWrapper.Companion.splitWords
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class SplitWordsTest {

  @Test
  fun `punctuation characters are included with their preceding words`() {

    "This has a comma, and also a period.".splitWords() shouldBe listOf(
      "This",
      "has",
      "a",
      "comma,",
      "and",
      "also",
      "a",
      "period."
    )
  }

  @Test
  fun `an inline code block is not split and retains its backticks`() {

    "before `code block` after".splitWords() shouldBe listOf(
      "before",
      "`code block`",
      "after"
    )
  }

  @Test
  fun `a comma after an inline code block stays with the code block`() {

    "before `code block`, after".splitWords() shouldBe listOf(
      "before",
      "`code block`,",
      "after"
    )
  }

  @Test
  fun `a period after an inline code block stays with the code block`() {

    "before `code block`. after".splitWords() shouldBe listOf(
      "before",
      "`code block`.",
      "after"
    )
  }

  @Test
  fun `a markdown link without parenthesis is not split and retains its brackets`() {

    "before [markdown link] after".splitWords() shouldBe listOf(
      "before",
      "[markdown link]",
      "after"
    )
  }

  @Test
  fun `a period after a markdown link without parenthesis stays with the link`() {

    "before [markdown link]. after".splitWords() shouldBe listOf(
      "before",
      "[markdown link].",
      "after"
    )
  }

  @Test
  fun `a character after a markdown link without parenthesis stays with the link`() {

    "before [markdown link]a after".splitWords() shouldBe listOf(
      "before",
      "[markdown link]a",
      "after"
    )
  }

  @Test
  fun `a comma after a markdown link without parenthesis stays with the link`() {

    "before [markdown link], after".splitWords() shouldBe listOf(
      "before",
      "[markdown link],",
      "after"
    )
  }

  @Test
  fun `a markdown link with parenthesis is not split and retains its brackets and parenthesis`() {

    "before [markdown link](actual link) after".splitWords() shouldBe listOf(
      "before",
      "[markdown link](actual link)",
      "after"
    )
  }

  @Test
  fun `a period after a markdown link with parenthesis stays with the link`() {

    "before [markdown link](actual link). after".splitWords() shouldBe listOf(
      "before",
      "[markdown link](actual link).",
      "after"
    )
  }

  @Test
  fun `a comma after a markdown link with parenthesis stays with the link`() {

    "before [markdown link](actual link), after".splitWords() shouldBe listOf(
      "before",
      "[markdown link](actual link),",
      "after"
    )
  }
}
