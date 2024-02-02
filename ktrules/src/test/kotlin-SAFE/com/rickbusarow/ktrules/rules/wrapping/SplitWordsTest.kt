/*
 * Copyright (C) 2024 Rick Busarow
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

import com.rickbusarow.kase.asContainers
import com.rickbusarow.kase.kase
import com.rickbusarow.ktrules.compat.EditorConfigProperty
import com.rickbusarow.ktrules.rules.Tests
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper.Companion.splitWords
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class SplitWordsTest : Tests<Unit> {

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
  fun `nested square brackets are all in a single group`() {

    "before [link1 and [link2]] after".splitWords() shouldBe listOf(
      "before",
      "[link1 and [link2]]",
      "after"
    )
  }

  @Test
  fun `nested square brackets in an inline-style link are all in a single group`() {

    "before [link1 and [link2]](actual link) after".splitWords() shouldBe listOf(
      "before",
      "[link1 and [link2]](actual link)",
      "after"
    )
  }

  @Test
  fun `consecutive wrapped groups are split individually`() {

    "before [link1] and [link2] after".splitWords() shouldBe listOf(
      "before",
      "[link1]",
      "and",
      "[link2]",
      "after"
    )
  }

  @TestFactory
  fun `single markdown delimiters are not dropped`() = listOf(
    kase("asterisk", '*'),
    kase("underscore", '_'),
    kase("tilde", '~'),
    kase("backtick", '`'),
    kase("open_square_bracket", '['),
    kase("close_square_bracket", ']'),
    kase("open_parenthesis", '('),
    kase("close_parenthesis", ')')
  )
    .asContainers { (character) ->
      listOf(
        kase("start", "${character}word"),
        kase("middle", "wo${character}rd"),
        kase("end", "word$character")
      ).asTests { (text) ->

        "before $text after".splitWords() shouldBe listOf(
          "before",
          text,
          "after"
        )
      }
    }

  @Test
  fun `bold with a space and apostrophe is not split`() {

    "before **dog cat's** after".splitWords() shouldBe listOf("before", "**dog cat's**", "after")
  }

  @Test
  fun `text wrapped in triple backticks is not split`() {

    "before ```dog cat's``` after".splitWords() shouldBe listOf(
      "before",
      "```dog cat's```",
      "after"
    )
  }

  @Test
  fun `a word with delimiters in the middle is not split`() {

    "before my__word__ after".splitWords() shouldBe listOf(
      "before",
      "my__word__",
      "after"
    )
  }

  @TestFactory
  fun `bold, italic, or strikethrough delimiters cannot have a whitespace immediately inside`() =
    listOf(
      "triple asterisks" to "***",
      "double asterisks" to "**",
      "single asterisks" to "*",
      "triple underscores" to "___",
      "double underscores" to "__",
      "single underscores" to "_",
      "double tildes" to "~~",
      "single tildes" to "~"
    ).container(
      { it.first }
    ) { (_, delim) ->

      listOf(
        test("space after opening delimiter") {
          "before $delim some words$delim after".splitWords() shouldBe listOf(
            "before",
            delim,
            "some",
            "words$delim",
            "after"
          )
        },
        test("space before closing delimiter") {
          "before ${delim}some words $delim after".splitWords() shouldBe listOf(
            "before",
            "${delim}some",
            "words",
            delim,
            "after"
          )
        }
      )
    }

  @Nested
  inner class `link splitting` {

    @TestFactory
    fun `links should not be split up`() = listOf(
      "reference style" to "[markdown link]",
      "shortcut style" to "[markdown link][actual link]",
      "inline style" to "[markdown link](actual link)"
    ).test({ it.first }) { (_, text) ->
      "before $text after".splitWords() shouldBe listOf(
        "before",
        text,
        "after"
      )
    }

    @TestFactory
    fun `links followed by a period should not be split up`() = listOf(
      "reference style" to "[markdown link].",
      "shortcut style" to "[markdown link][actual link].",
      "inline style" to "[markdown link](actual link)."
    ).test({ it.first }) { (_, text) ->

      "before $text after".splitWords() shouldBe listOf(
        "before",
        text,
        "after"
      )
    }

    @TestFactory
    fun `links followed by a comma should not be split up`() = listOf(
      "reference style" to "[markdown link],",
      "shortcut style" to "[markdown link][actual link],",
      "inline style" to "[markdown link](actual link),"
    ).test({ it.first }) { (_, text) ->

      "before $text after".splitWords() shouldBe listOf(
        "before",
        text,
        "after"
      )
    }

    @TestFactory
    fun `links followed by more text without a space should not be split up`() = listOf(
      "reference style" to "[markdown link]word",
      "shortcut style" to "[markdown link][actual link]word",
      "inline style" to "[markdown link](actual link)word"
    ).test({ it.first }) { (_, text) ->

      "before $text after".splitWords() shouldBe listOf(
        "before",
        text,
        "after"
      )
    }
  }

  override fun eco(vararg properties: Pair<EditorConfigProperty<*>, *>) {
    TODO("Not yet implemented")
  }
}
