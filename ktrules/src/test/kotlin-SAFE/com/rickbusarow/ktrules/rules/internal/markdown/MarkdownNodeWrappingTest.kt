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

package com.rickbusarow.ktrules.rules.internal.markdown

import com.rickbusarow.ktrules.rules.Tests
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.noDots
import com.rickbusarow.ktrules.rules.wrapping.GreedyWrapper
import com.rickbusarow.ktrules.rules.wrapping.MinimumRaggednessWrapper
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper
import org.intellij.lang.annotations.Language
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.junit.jupiter.api.Test

@Suppress("SpellCheckingInspection")
class MarkdownNodeWrappingTest : Tests {

  @Test
  fun `threshold wrapping`() {

    wrap(
      """
        |@property extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
        |    desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
      """.trimMargin()
    ) shouldBe """
        |@property extercitatrekvsuion nostrud
        |  exerc mco laboris nisteghi ut aliquip
        |  ex ea desegrunt fugiat nulla pariatur.
        |  Excepteur sint occaecat cupidatat
    """.trimMargin()
  }

  @Test
  fun `text with a link and no extra spaces is left alone`() {

    wrap(
      """
        |comment with [Subject]
      """.trimMargin()
    ) shouldBe
      """
        |comment with [Subject]
      """.trimMargin()
  }

  @Test
  fun `an code fenced block with language in the default section is treated as a code block`() {

    wrap(
      """
        |a comment
        |
        |```kotlin
        |fun foo() = Unit
        |val result = foo()
        |```
        |
        |more comment
      """.trimMargin()
    ) shouldBe """
        |a comment
        |
        |```kotlin
        |fun foo() = Unit
        |val result = foo()
        |```
        |
        |more comment
    """.trimMargin()
  }

  @Test
  fun `an code fenced block without language in the default section is treated as a code block`() {

    wrap(
      """
        |a comment
        |
        |```
        |fun foo() = Unit
        |val result = foo()
        |```
      """.trimMargin()
    ) shouldBe """
        |a comment
        |
        |```
        |fun foo() = Unit
        |val result = foo()
        |```
    """.trimMargin()
  }

  @Test
  fun `an indented paragraph in the default section is treated as a code block`() {

    wrap(
      """
        |a comment
        |
        |    fun foo() = Unit
        |    val result = foo()
        |
        |more comment
      """.trimMargin()
    ) shouldBe """
        |a comment
        |
        |    fun foo() = Unit
        |    val result = foo()
        |
        |more comment
    """.trimMargin()
  }

  @Test
  fun `a single line indented code block is treated as a code block`() {

    wrap(
      """
        |a comment
        |
        |    object Foo
        |
        |more comment
      """.trimMargin()
    ) shouldBe """
        |a comment
        |
        |    object Foo
        |
        |more comment
    """.trimMargin()
  }

  @Test
  fun `an indented code block with a blank line in the middle does not have trailing spaces`() {

    wrap(
      """
        |Some normal line.
        |
        |    object Foo
        |
        |    object Bar
        |
        |Another normal line
      """.trimMargin()
    ) shouldBe """
        |Some normal line.
        |
        |    object Foo
        |
        |    object Bar
        |
        |Another normal line
    """.trimMargin()
  }

  @Test
  fun `an indented tag comment second paragraph is wrapped and indented on each line`() {

    wrap(
      """
        |@property name some name property
        |
        |  Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
        |  incididunt.
      """.trimMargin()
    ) shouldBe """
        |@property name some name property
        |
        |  Lorem ipsum dolor sit amet,
        |  consectetur adipiscing elit, sed
        |  do eiusmod tempor incididunt.
    """.trimMargin()
  }

  @Test
  fun `a paragraph after a tag comment is indented by two spaces`() {

    wrap(
      """
        |@property name some name property
        |
        |Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut
        |labore et dolore magna aliqua.
      """.trimMargin()
    ) shouldBe """
        |@property name some name property
        |
        |  Lorem ipsum dolor sit amet, consectetur
        |  adipiscing elit, sed do eiusmod tempor
        |  incididunt ut labore et dolore magna aliqua.
    """.trimMargin()
  }

  @Test
  fun `a long single-line tag description is wrapped and indented`() {

    wrap(
      """
        |@property name Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor.
      """.trimMargin()
    ) shouldBe """
        |@property name Lorem ipsum dolor
        |  sit amet, consectetur adipiscing
        |  elit, sed do eiusmod tempor.
    """.trimMargin()
  }

  @Test
  fun `code blocks are not wrapped`() {

    wrap(
      """
        |Given this
        |code:
        |
        |```java
        |val seq = TODO()
        |```
        |followed by:
        |```
        |fun foo() = Unit
        |```
        |do some things
      """.trimMargin()
    ) shouldBe """
        |Given this code:
        |
        |```java
        |val seq = TODO()
        |```
        |followed by:
        |```
        |fun foo() = Unit
        |```
        |do some things
    """.trimMargin()
  }

  @Test
  fun `a code block which is exactly 4 spaces from the asterisk is not collapsed`() {

    wrap(
      """
      |This is a comment.
      |
      |    data class Student(
      |      val name: String
      |    )
      |
      |This is also a comment.
      """.trimMargin(),
      maxLengh = 100
    ) shouldBe """
      |This is a comment.
      |
      |    data class Student(
      |      val name: String
      |    )
      |
      |This is also a comment.
    """.trimMargin()
  }

  @Test
  fun `a code block which is exactly 5 spaces from the asterisk is not collapsed`() {

    wrap(
      """
      |This is a comment.
      |
      |     data class Student(
      |       val name: String
      |     )
      |
      |This is also a comment.
      """.trimMargin(),
      maxLengh = 100
    ) shouldBe """
      |This is a comment.
      |
      |     data class Student(
      |       val name: String
      |     )
      |
      |This is also a comment.
    """.trimMargin()
  }

  @Test
  fun `a fenced code block with 4+ indented spaces does not have its indentation changed`() {

    wrap(
      """
      |One two three four five six
      |
      |```
      |sequence {
      |    yield(5)
      |}
      |```
      """.trimMargin(),
      maxLengh = 25
    ) shouldBe """
      |One two three
      |four five six
      |
      |```
      |sequence {
      |    yield(5)
      |}
      |```
    """.trimMargin()
  }

  @Test
  fun `minimum raggedness wrapping with maximum line length`() {

    wrap(
      """
      |This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
      |
      |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
      """.trimMargin(),
      wrapper = MinimumRaggednessWrapper(),
      maxLengh = 27
    ) shouldBe """
      |This is a test with some
      |verylongwordsthatexceedthelinelimit
      |and
      |`text wrapped in backticks`.
      |
      |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
    """.trimMargin()
  }

  @Test
  fun `greedy wrapping with short line length`() {

    wrap(
      """
        |This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
        |
        |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
      """.trimMargin(),
      wrapper = GreedyWrapper(),
      maxLengh = 5
    ) shouldBe """
        |This
        |is a
        |test
        |with
        |some
        |verylongwordsthatexceedthelinelimit
        |and
        |`text wrapped in backticks`.
        |
        |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
    """.trimMargin()
  }

  @Test
  fun `minimum raggedness wrapping with short line length`() {

    wrap(
      """
        |This is a test with some verylongwordsthatexceedthelinelimit and `text wrapped in backticks`.
        |
        |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
      """.trimMargin(),
      wrapper = MinimumRaggednessWrapper(),
      maxLengh = 5
    ) shouldBe """
        |This
        |is a
        |test
        |with
        |some
        |verylongwordsthatexceedthelinelimit
        |and
        |`text wrapped in backticks`.
        |
        |[This is a very long Markdown link that should not be wrapped](https://www.example.com/some/very/long/url)
    """.trimMargin()
  }

  @Test
  fun `an indented code block with code fences is left alone`() {

    wrap(
      """
        |A comment
        |
        |    ```
        |    // some code
        |    ```
      """.trimMargin()
    ) shouldBe
      """
        |A comment
        |
        |    ```
        |    // some code
        |    ```
      """.trimMargin()
  }

  @Test
  fun `a numbered list in the default section with a long paragraph is wrapped`() {

    wrap(
      """
        |My list:
        |
        |1. item 1 is a long paragraph which is too long for a single line.
        |2. item 2
      """.trimMargin()
    ) shouldBe
      """
        |My list:
        |
        |1. item 1 is a long paragraph which
        |   is too long for a single line.
        |2. item 2
      """.trimMargin()
  }

  @Test
  fun `a numbered list increases continuation indent when the number is longer`() {

    wrap(
      """
        |1. one
        |2. two
        |3. three
        |4. four
        |5. five
        |   1. five-one
        |   2. five-two
        |6. size
        |7. seven
        |8. a b c d e f g h i j k l m n o p q r s t u v w x y z
        |9. nine
        |10. aa b c d e f g h i j k l
        |    m n o p q r s t u v w x y z
        |
        |    extra line for item ten
        |    1. a b c d e f g h i j k l m n o p q r s t u v w x y z
        |    2. ten-two
        |11. eleven
      """.trimMargin(),
      maxLengh = 50,
      wrapper = GreedyWrapper()
    ) shouldBe
      """
        |1. one
        |2. two
        |3. three
        |4. four
        |5. five
        |   1. five-one
        |   2. five-two
        |6. size
        |7. seven
        |8. a b c d e f g h i j k l m n o p q r s t u v w x
        |   y z
        |9. nine
        |10. aa b c d e f g h i j k l m n o p q r s t u v w
        |    x y z
        |
        |    extra line for item ten
        |    1. a b c d e f g h i j k l m n o p q r s t u v
        |       w x y z
        |    2. ten-two
        |11. eleven
      """.trimMargin()
  }

  @Test
  fun `a bulleted list in the default section is not wrapped`() {

    wrap(
      """
        |My list:
        |
        |- item 1
        |- item 2
      """.trimMargin()
    ) shouldBe
      """
        |My list:
        |
        |- item 1
        |- item 2
      """.trimMargin()
  }

  @Test
  fun `a bulleted list in a property tag is not wrapped`() {

    wrap(
      """
        |@property name My list:
        |
        |  - item 1
        |  - item 2
      """.trimMargin()
    ) shouldBe """
        |@property name My list:
        |
        |  - item 1
        |  - item 2
    """.trimMargin()
  }

  @Test
  fun `a bulleted list in a property tag is indented`() {

    wrap(
      """
        |@property name My list:
        |
        |- item 1
        |- item 2
      """.trimMargin()
    ) shouldBe """
        |@property name My list:
        |
        |  - item 1
        |  - item 2
    """.trimMargin()
  }

  @Test
  fun `a nested bulleted list in a property tag is indented`() {

    wrap(
      """
        |@property name My list:
        |
        |- item 1
        |- item 2
        |  - item 3
        |- item 4
      """.trimMargin()
    ) shouldBe """
        |@property name My list:
        |
        |  - item 1
        |  - item 2
        |    - item 3
        |  - item 4
    """.trimMargin()
  }

  @Test
  fun `a nested numbered list in a property tag is indented`() {

    wrap(
      """
        |@property name My list:
        |
        |1. item 1
        |2. item 2
        |   1. item a
        |3. item 3
      """.trimMargin()
    ) shouldBe """
        |@property name My list:
        |
        |  1. item 1
        |  2. item 2
        |     1. item a
        |  3. item 3
    """.trimMargin()
  }

  @Test
  fun `a markdown table in the default section is not wrapped`() {

    wrap(
      """
        |My table:
        |
        || one | two |
        ||:---:|:---:|
        ||  a  |  b  |
        ||  c  |  d  |
      """.trimMargin()
    ) shouldBe """
        |My table:
        |
        || one | two |
        ||:---:|:---:|
        ||  a  |  b  |
        ||  c  |  d  |
    """.trimMargin()
  }

  @Test
  fun `a markdown table in a property tag is not wrapped`() {

    wrap(
      """
        |@property name My table:
        |
        |  | one | two |
        |  |:---:|:---:|
        |  |  a  |  b  |
        |  |  c  |  d  |
      """.trimMargin()
    ) shouldBe
      """
        |@property name My table:
        |
        |  | one | two |
        |  |:---:|:---:|
        |  |  a  |  b  |
        |  |  c  |  d  |
      """.trimMargin()
  }

  @Test
  fun `a block quote in the default section is wrapped`() {

    wrap(
      """
      |My quote:
      |
      |> This is a long sentence which should be wrapped when the line length is shorter.
      """.trimMargin()
    ) shouldBe """
      |My quote:
      |
      |> This is a long sentence which should be
      |> wrapped when the line length is shorter.
    """.trimMargin()
  }

  @Test
  fun `a block quote with one leading space keeps that space`() {

    wrap(
      """
      |My quote:
      |
      | > This is a quote
      """.trimMargin()
    ) shouldBe """
      |My quote:
      |
      | > This is a quote
    """.trimMargin()
  }

  @Test
  fun `a block quote which could be unwrapped is unwrapped`() {

    wrap(
      """
        |My quote:
        |
        |> This
        |> is a sentence.
      """.trimMargin()
    ) shouldBe """
        |My quote:
        |
        |> This is a sentence.
    """.trimMargin()
  }

  @Test
  fun `property tags after a paragraph and a newline stay where they are`() {

    wrap(
      """
       |A paragraph
       |to unwrap
       |
       |@property name name_property
      """.trimMargin()
    ) shouldBe """
       |A paragraph to unwrap
       |
       |@property name name_property
    """.trimMargin()
  }

  @Test
  fun `a block quote in a property tag is wrapped`() {

    wrap(
      """
      |@property name My quote:
      |
      |  > This is a long sentence which should be wrapped when the line length is shorter.
      """.trimMargin()
    ) shouldBe """
      |@property name My quote:
      |
      |  > This is a long sentence which should be
      |  > wrapped when the line length is shorter.
    """.trimMargin()
  }

  @Test
  fun `a bulleted list at the start of the default section is wrapped`() {

    wrap(
      """
        |- bulleted list in default section line one
        |- bulleted list in default
        |
        |  section line two
        |- bulleted list in default section line three
      """.trimMargin(),
      maxLengh = 30
    ) shouldBe """
        |- bulleted list in default
        |  section line one
        |- bulleted list in default
        |
        |  section line two
        |- bulleted list in default
        |  section line three
    """.trimMargin()
  }

  internal fun wrap(
    @Language("markdown") markdown: String,
    defaultSection: Boolean = markdown.trimStart().matches("^\\s*[^@][\\s\\S]*".toRegex()),
    maxLengh: Int = 50,
    wrapper: StringWrapper = MinimumRaggednessWrapper()
  ): String = MarkdownNode.from(markdown.noDots, GFMFlavourDescriptor())
    .wrap(
      wrapper = wrapper,
      maxLength = maxLengh,
      beforeAnyTags = defaultSection,
      addKDocLeadingSpace = false
    )
    .dots
}
