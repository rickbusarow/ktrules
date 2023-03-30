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

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.api.EditorConfigOverride
import com.pinterest.ktlint.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import com.pinterest.ktlint.test.format as ktlintTestFormat
import com.pinterest.ktlint.test.lint as ktlintTestLint

@Suppress("SpellCheckingInspection")
class KDocWrappingRuleTest {

  val rules = setOf(
    RuleProvider { KDocWrappingRule() }
  )

  @Test
  fun `threshold wrapping`() {

    rules.format(
      """
      /**
       * @property extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       *     desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @property extercitatrekvsuion nostrud
       *   exerc mco laboris nisteghi ut aliquip
       *   ex ea desegrunt fugiat nulla pariatur.
       *   Excepteur sint occaecat cupidatat
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `@see tags have their extra white spaces removed but are not wrapped together`() {

    rules.format(
      """
      /**
       * First line
       *   second line
       *
       * @see SomeClass someClass     description
       * @see Object object     description
       * @since 0.10.0
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * First line second line
       *
       * @see SomeClass someClass description
       * @see Object object description
       * @since 0.10.0
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `text with a link and no extra spaces is left alone`() {

    rules.lint(
      """
      /**
       * comment with [Subject]
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe listOf()
  }

  @Test
  fun `an code fenced block with language in the default section is treated as a code block`() {

    rules.format(
      """
      /**
       * a comment
       *
       * ```kotlin
       * fun foo() = Unit
       * val result = foo()
       * ```
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       * ```kotlin
       * fun foo() = Unit
       * val result = foo()
       * ```
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an code fenced block without language in the default section is treated as a code block`() {

    rules.format(
      """
      /**
       * a comment
       *
       * ```
       * fun foo() = Unit
       * val result = foo()
       * ```
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       * ```
       * fun foo() = Unit
       * val result = foo()
       * ```
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an indented paragraph in the default section is treated as a code block`() {

    rules.format(
      """
      /**
       * a comment
       *
       *     fun foo() = Unit
       *     val result = foo()
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       *     fun foo() = Unit
       *     val result = foo()
       *
       * @property name some name property
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a single line indented code block is treated as a code block`() {

    rules.format(
      """
      /**
       * a comment
       *
       *    object Foo
       *
       * @property name a name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       *    object Foo
       *
       * @property name a name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an indented code block with a blank line in the middle does not have trailing spaces`() {

    rules.format(
      """
      /**
       * Some normal line.
       *
       *     object Foo
       *
       *     object Bar
       *
       * Another normal line
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * Some normal line.
       *
       *     object Foo
       *
       *     object Bar
       *
       * Another normal line
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an indented tag comment is moved left`() {

    rules.format(
      """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       *                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
       *                incididunt.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       *   Lorem ipsum dolor sit amet,
       *   consectetur adipiscing elit, sed
       *   do eiusmod tempor incididunt.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a paragraph after a tag comment is indented by two spaces`() {

    rules.format(
      """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut
       * labore et dolore magna aliqua.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       *   Lorem ipsum dolor sit amet,
       *   consectetur adipiscing elit, sed
       *   do eiusmod tempor incididunt ut
       *   labore et dolore magna aliqua.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a long single-line tag description is wrapped and indented`() {

    rules.format(
      """
      /**
       * a comment
       *
       * @property name Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * a comment
       *
       * @property name Lorem ipsum dolor
       *   sit amet, consectetur adipiscing
       *   elit, sed do eiusmod tempor.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a correctly formatted kdoc does not emit`() {

    rules.lint(
      """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name Lorem ipsum
       *   dolor sit amet, consectetur.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ).shouldBeEmpty()
  }

  @Test
  fun `code blocks are not wrapped`() {

    rules.format(
      """
      /**
       * Given this
       * code:
       *
       * ```java
       * val seq = TODO()
       * ```
       * followed by:
       * ```
       * fun foo() = Unit
       * ```
       * do some things
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * Given this code:
       *
       * ```java
       * val seq = TODO()
       * ```
       *
       * followed by:
       *
       * ```
       * fun foo() = Unit
       * ```
       *
       * do some things
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a code block which is exactly 4 spaces from the asterisk is not collapsed`() {

    rules.format(
      """
      /**
       * This is a comment.
       *
       *    data class Student(
       *      val name: String
       *    )
       *
       * This is also a comment.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent(),
      lineLength = 100
    ) shouldBe """
      /**
       * This is a comment.
       *
       *    data class Student(
       *      val name: String
       *    )
       *
       * This is also a comment.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a code block which is exactly 5 spaces from the asterisk is not collapsed`() {

    rules.format(
      """
      /**
       * This is a comment.
       *
       *     data class Student(
       *       val name: String
       *     )
       *
       * This is also a comment.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent(),
      lineLength = 100
    ) shouldBe """
      /**
       * This is a comment.
       *
       *     data class Student(
       *       val name: String
       *     )
       *
       * This is also a comment.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a fenced code block with 4+ indented spaces does not have its indentation changed`() {

    rules.format(
      """
      /**
       * One two three four five six
       *
       * ```
       * sequence {
       *     yield(5)
       * }
       * ```
       */
      class Subject
      """.trimIndent(),
      lineLength = 25
    ) shouldBe """
      /**
       * One two three
       * four five six
       *
       * ```
       * sequence {
       *     yield(5)
       * }
       * ```
       */
      class Subject
    """.trimIndent()
  }

  @Test
  fun `minimum raggedness wrapping with maximum line length`() {

    rules.format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.MINIMUM_RAGGED,
      lineLength = 27
    ) shouldBe """
      /**
       * This is a test with some
       * verylongwordsthatexceedthelinelimit
       * and
       * `text wrapped in backticks`.
       *
       * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
       */
      class TestClass
    """.trimIndent()
  }

  @Test
  fun `greedy wrapping with short line length`() {

    rules.format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.GREEDY,
      lineLength = 7
    ) shouldBe """
      /**
       * This
       * is a
       * test
       * with
       * some
       * verylongwordsthatexceedthelinelimit
       * and
       * `text wrapped in backticks`.
       *
       * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
       */
      class TestClass
    """.trimIndent()
  }

  @Test
  fun `minimum raggedness wrapping with short line length`() {

    rules.format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.MINIMUM_RAGGED,
      lineLength = 7
    ) shouldBe """
      /**
       * This
       * is a
       * test
       * with
       * some
       * verylongwordsthatexceedthelinelimit
       * and
       * `text wrapped in backticks`.
       *
       * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
       */
      class TestClass
    """.trimIndent()
  }

  @Test
  fun `an indented code block with code fences is left alone`() {

    val results = rules.lint(
      text = """
        /**
         * A comment
         *
         *     ```
         *     // some code
         *     ```
         */
        class TestClass
      """.trimIndent(),
    )
    results shouldBe listOf()
  }

  @Test
  fun `a bulleted list in the default section is not wrapped`() {

    val results = rules.lint(
      text = """
      /**
       * My list:
       *
       * - item 1
       * - item 2
       */
      class TestClass
      """.trimIndent(),
    )
    results shouldBe listOf()
  }

  @Test
  fun `a bulleted list in a property tag is not wrapped`() {

    val results = rules.lint(
      text = """
      /**
       * @property name My list:
       *
       *   - item 1
       *   - item 2
       */
      class TestClass(val name: String)
      """.trimIndent(),
    )
    results shouldBe listOf()
  }

  @Test
  fun `a markdown table in the default section is not wrapped`() {

    val results = rules.lint(
      text = """
      /**
       * My table:
       *
       * | one | two |
       * |:---:|:---:|
       * |  a  |  b  |
       * |  c  |  d  |
       */
      class TestClass(val name: String)
      """.trimIndent(),
    )
    results shouldBe listOf()
  }

  @Test
  fun `a markdown table in a property tag is not wrapped`() {

    val results = rules.lint(
      text = """
      /**
       * @property name My table:
       *
       *   | one | two |
       *   |:---:|:---:|
       *   |  a  |  b  |
       *   |  c  |  d  |
       */
      class TestClass(val name: String)
      """.trimIndent(),
    )
    results shouldBe listOf()
  }

  private fun Set<RuleProvider>.format(
    @Language("kotlin")
    text: String,
    wrappingStyle: WrappingStyle = WrappingStyle.MINIMUM_RAGGED,
    lineLength: Int = 50,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): String = ktlintTestFormat(
    text = text,
    filePath = null,
    editorConfigOverride = editorConfigOverride,
  )
    .first

  private fun Set<RuleProvider>.lint(
    @Language("kotlin")
    text: String,
    wrappingStyle: WrappingStyle = WrappingStyle.MINIMUM_RAGGED,
    lineLength: Int = 50,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): List<LintError> = ktlintTestLint(
    text = text,
    editorConfigOverride = editorConfigOverride
  )
}
