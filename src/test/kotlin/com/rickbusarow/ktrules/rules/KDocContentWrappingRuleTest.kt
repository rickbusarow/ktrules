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

import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.rules.Tests.KtLintTestResult
import com.rickbusarow.ktrules.rules.WrappingStyle.GREEDY
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@Suppress("SpellCheckingInspection")
class KDocContentWrappingRuleTest : Tests {

  override val rules = setOf(
    RuleProviderCompat { KDocContentWrappingRule() }
  )

  @Test
  fun `threshold wrapping`() {

    format(
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
    ) {
      expectError(2, 4)

      output shouldBe """
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
  }

  @Test
  fun `a default section collapsed comment which is too long is wrapped`() {

    format(
      """
      /** extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex eacupidatat */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(1, 4)

      output shouldBe """
      /**
       * extercitatrekvsuion nostrud exerc mco
       * laboris nisteghi ut aliquip ex eacupidatat
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a collapsed tag which is too long is wrapped`() {

    format(
      """
      /** @property extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex eacupidatat */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(1, 5)

      output shouldBe """
        /**
         * @property extercitatrekvsuion nostrud exerc mco
         *   laboris nisteghi ut aliquip ex eacupidatat
         */
        data class Subject(
          val name: String,
          val age: Int
        )
      """.trimIndent()
    }
  }

  @Test
  fun `greedy threshold wrapping`() {

    format(
      """
      /**
       * comment
       *
       * ohrpl tieiecec as,tPaclfa]swa' aoAaAfeht so sccySai Seae pAeRr
       * en sGieotfe wri lfw[espvtgcA
       * g caa teths t tpnthhiyhlxsat.ee si
       *
       * 1. aapwAPatSnhtar cfecat lcipge creaAe
       *    t esif
       *    swisAycoGvaf] eeoleods
       *    sie ei,o [rRShcp'lAtwae ssfe
       *    g caa teths
       *    t tpnthhiyhlxsat.ee si
       *
       * @since 0.12.0
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent(),
      lineLength = 100,
      wrappingStyle = GREEDY
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * comment
       *
       * ohrpl tieiecec as,tPaclfa]swa' aoAaAfeht so sccySai Seae pAeRr en sGieotfe wri lfw[espvtgcA g caa
       * teths t tpnthhiyhlxsat.ee si
       *
       * 1. aapwAPatSnhtar cfecat lcipge creaAe t esif swisAycoGvaf] eeoleods sie ei,o [rRShcp'lAtwae ssfe
       *    g caa teths t tpnthhiyhlxsat.ee si
       *
       * @since 0.12.0
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `text with a link and no extra spaces is left alone`() {

    lint(
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

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `an code fenced block without language in the default section is treated as a code block`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `an indented paragraph in the default section is treated as a code block`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `a single line indented code block is treated as a code block`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `an indented code block with a blank line in the middle does not have trailing spaces`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `an indented tag comment is moved left`() {

    format(
      """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       *   Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
       *   incididunt.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(6, 4)

      output shouldBe """
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
  }

  @Test
  fun `a paragraph after a tag comment is indented by two spaces`() {

    format(
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
    ) {
      expectError(6, 4)

      output shouldBe """
      /**
       * a comment
       *
       * A new paragraph.
       *
       * @property name some name property
       *
       *   Lorem ipsum dolor sit amet, consectetur
       *   adipiscing elit, sed do eiusmod tempor
       *   incididunt ut labore et dolore magna aliqua.
       * @property age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a long single-line tag description is wrapped and indented`() {

    format(
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
    ) {
      expectError(4, 4)

      output shouldBe """
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
  }

  @Test
  fun `a correctly formatted kdoc does not emit`() {

    lint(
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

    format(
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
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * Given this code:
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
    }
  }

  @Test
  fun `a code block which is exactly 4 spaces from the asterisk is not collapsed`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `a code block which is exactly 5 spaces from the asterisk is not collapsed`() {

    format(
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
    ) {

      output shouldBe """
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
  }

  @Test
  fun `a fenced code block with 4+ indented spaces does not have its indentation changed`() {

    format(
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
    ) {
      expectError(2, 2)

      output shouldBe """
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
  }

  @Test
  fun `minimum raggedness wrapping with maximum line length`() {

    format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.MINIMUM_RAGGED,
      lineLength = 28
    ) {
      expectError(2, 2)

      output shouldBe """
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
  }

  @Test
  fun `greedy wrapping with short line length`() {

    format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.GREEDY,
      lineLength = 8
    ) {
      expectError(2, 2)

      output shouldBe """
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
  }

  @Test
  fun `minimum raggedness wrapping with short line length`() {

    format(
      text = """
        /**
         * This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
         *
         * [This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
         */
        class TestClass
      """.trimIndent(),
      wrappingStyle = WrappingStyle.MINIMUM_RAGGED,
      lineLength = 8
    ) {
      expectError(2, 2)

      output shouldBe """
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
  }

  @Test
  fun `an indented code block with code fences is left alone`() {

    val results = lint(
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

    val results = lint(
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

    val results = lint(
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

    val results = lint(
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

    val results = lint(
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

  @Test
  fun `a single bullet point list in the default section is wrapped`() {

    format(
      text = """
        /**
         * My outline:
         *
         * - This is a long sentence which should be wrapped when the line length is shorter.
         */
        class TestClass(val name: String)
      """.trimIndent(),
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * My outline:
       *
       * - This is a long sentence which should be
       *   wrapped when the line length is shorter.
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `a block quote in the default section is wrapped`() {

    format(
      text = """
        /**
         * My quote:
         *
         * > This is a long sentence which should be wrapped when the line length is shorter.
         */
        class TestClass(val name: String)
      """.trimIndent(),
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * My quote:
       *
       * > This is a long sentence which should be
       * > wrapped when the line length is shorter.
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `a default comment with tag which is already correct does not throw`() {

    lint(
      text = """
        /**
         * The location in the local file system to which the root of the repository was mapped at the
         * time of the analysis.
         *
         * @since 0.12.0
         */
        class TestClass(val name: String)
      """.trimIndent(),
      lineLength = 95,
      wrappingStyle = GREEDY
    ) shouldBe emptyList()
  }

  @Test
  fun `a single-line default section kdoc is left alone`() {

    lint(
      text = """
        /** Comment */
        class TestClass(val name: String)
      """.trimIndent(),
    ) shouldBe emptyList()
  }

  @Test
  fun `a single-line tag kdoc is left alone`() {

    lint(
      text = """
        /** @since 0.0.1 */
        class TestClass(val name: String)
      """.trimIndent(),
    ) shouldBe emptyList()
  }

  @Test
  fun `a block quote which could be unwrapped is unwrapped`() {

    format(
      text = """
        /**
         * My quote:
         *
         * > This
         * > is a sentence.
         */
        class TestClass(val name: String)
      """.trimIndent(),
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * My quote:
       *
       * > This is a sentence.
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `a block quote in a property tag is wrapped`() {

    format(
      text = """
      /**
       * @property name My quote:
       *
       *   > This is a long sentence which should be wrapped when the line length is shorter.
       */
      class TestClass(val name: String)
      """.trimIndent(),
    ) {
      expectError(2, 4)

      output shouldBe """
      /**
       * @property name My quote:
       *
       *   > This is a long sentence which should be
       *   > wrapped when the line length is shorter.
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `a bulleted list at the start of the default section is wrapped`() {

    format(
      text = """
      /**
       * - bulleted list in default section line one
       * - bulleted list in default
       *
       *   section line two
       * - bulleted list in default section line three
       */
      class TestClass(val name: String)
      """.trimIndent(),
      lineLength = 30
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * - bulleted list in default
       *   section line one
       * - bulleted list in default
       *
       *   section line two
       * - bulleted list in default
       *   section line three
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `property tags after a paragraph and a newline stay where they are`() {

    format(
      text = """
      /**
       * A paragraph
       * to unwrap
       *
       * @property name  name_property
       */
      class TestClass(val name: String)
      """.trimIndent()
    ) {
      expectError(2, 2)
      expectError(5, 1)

      output shouldBe """
      /**
       * A paragraph to unwrap
       *
       * @property name name_property
       */
      class TestClass(val name: String)
      """.trimIndent()
    }
  }

  @Test
  fun `a tag without a blank line after the default section paragraph is not wrapped`() {

    format(
      """
      /**
       * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.
       * @param age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent(),
      lineLength = 60,
      wrappingStyle = GREEDY
    ) {
      expectError(2, 2)

      output shouldBe """
      /**
       * Lorem ipsum dolor sit amet, consectetur adipiscing elit,
       * sed do eiusmod tempor.
       * @param age some age property
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `@see tags have their extra white spaces removed but are not wrapped together`() {

    format(
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
    ) {
      expectError(2, 2)
      expectError(4, 2)
      expectError(5, 39)

      output shouldBe """
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
  }

  @Test
  fun `a list inside a property tag is indented`() {

    format(
      """
      /**
       * @property name name_description
       *
       * - item 1
       * - item 2
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(2, 4)

      output shouldBe """
      /**
       * @property name name_description
       *
       *   - item 1
       *   - item 2
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single-line kdoc does not have an asterisk added`() {

    lint(
      """
      /** comment */
      class Subject
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `a single-line tagged kdoc does not have an asterisk added`() {

    lint(
      """
      /** @orange tangerine */
      class Subject
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `wrapping the first tag with only unknown tags`() {

    format(
      """
      /**
       * @return it returns something with a very long description because I need to wrap
       * @since 0.0.1
       * @throws IllegalArgumentException because I
       *   need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(2, 4)

      output shouldBe """
      /**
       * @return it returns something with a very
       *   long description because I need to wrap
       * @since 0.0.1
       * @throws IllegalArgumentException because I
       *   need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `wrapping a middle tag with only unknown tags`() {

    format(
      """
      /**
       * @return it returns something
       * @since A very very very very very very very very very very very very very long time ago
       * @throws IllegalArgumentException because I
       *   need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(3, 4)

      output shouldBe """
      /**
       * @return it returns something
       * @since A very very very very very very very
       *   very very very very very very long time ago
       * @throws IllegalArgumentException because I
       *   need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `wrapping the last tag with only unknown tags`() {

    format(
      """
      /**
       * @return it returns something
       * @since 0.1.1
       * @throws IllegalArgumentException because I need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 4)

      output shouldBe """
      /**
       * @return it returns something
       * @since 0.1.1
       * @throws IllegalArgumentException because I
       *   need a comment which is long enough to wrap
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  private fun KtLintTestResult.expectError(
    line: Int,
    col: Int,
  ) {
    expectError(
      line = line,
      col = col,
      ruleId = KDocContentWrappingRule.ID,
      detail = KDocContentWrappingRule.ERROR_MESSAGE
    )
  }
}
