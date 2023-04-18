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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import com.pinterest.ktlint.test.format as ktlintTestFormat
import com.pinterest.ktlint.test.lint as ktlintTestLint

class KDocCollapseRuleTest : Tests {

  val rules = setOf(
    RuleProvider { KDocCollapseRule() }
  )

  @Test
  fun `a default section with two lines of text is not collapsed`() {

    rules.lint(
      """
      /**
       * line one
       * line two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe emptyList()
  }

  @Test
  fun `the kdoc is not collapsed if its collapsed length is one char beyond the max length`() {

    rules.lint(
      """
      /**
       * This comment is 62 characters long when it's collapsed.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """,
      lineLength = 61
    ) shouldBe emptyList()
  }

  @Test
  fun `the kdoc is collapsed if its collapsed length is exactly the max length`() {

    rules.format(
      """
      /**
       * This comment is 62 characters long when it's collapsed.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """,
      lineLength = 62
    ) shouldBe """
      /** This comment is 62 characters long when it's collapsed. */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `the kdoc is collapsed if its collapsed length is exactly one less than the max length`() {

    rules.format(
      """
      /**
       * This comment is 62 characters long when it's collapsed.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """,
      lineLength = 63
    ) shouldBe """
      /** This comment is 62 characters long when it's collapsed. */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a default section 4-space-indent code block is collapsed but not trimmed`() {

    rules.format(
      """
      /**
       *    paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /**    paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a default section with leading and trailing newlines is collapsed`() {

    rules.format(
      """
      /**
       * paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a default section with a leading newline is collapsed`() {

    rules.format(
      """
      /**
       * paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a default section with a trailing newline is collapsed`() {

    rules.format(
      """
      /** paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an unknown tag with leading and trailing newlines is collapsed`() {

    rules.format(
      """
      /**
       * @orange banana
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an unknown tag with a leading newline is collapsed`() {

    rules.format(
      """
      /**
       * @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an unknown tag with a leading newline and no trailing space is collapsed`() {

    rules.format(
      """
      /**
       * @orange banana*/
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `an unknown tag with a trailing newline is collapsed`() {

    rules.format(
      """
      /** @orange banana
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a known tag with leading and trailing newlines is collapsed`() {

    rules.format(
      """
      /**
       * @property name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a known tag with a leading newline is collapsed`() {

    rules.format(
      """
      /**
       * @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a known tag with a leading newline and no trailing space is collapsed`() {

    rules.format(
      """
      /**
       * @property name*/
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a known tag with a trailing newline is collapsed`() {

    rules.format(
      """
      /** @property name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  private fun Set<RuleProvider>.format(
    text: String,
    wrappingStyle: WrappingStyle = WrappingStyle.MINIMUM_RAGGED,
    lineLength: Int = 50,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): String = ktlintTestFormat(
    text = text.trimIndent(),
    filePath = null,
    editorConfigOverride = editorConfigOverride,
  )
    .first

  override fun Set<RuleProvider>.format(
    text: String,
    filePath: String?,
    editorConfigOverride: EditorConfigOverride
  ): String = ktlintTestFormat(
    text = text.trimIndent(),
    filePath = filePath,
    editorConfigOverride = if (editorConfigOverride.properties.isEmpty()) {
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to 50,
        WRAPPING_STYLE_PROPERTY to WrappingStyle.MINIMUM_RAGGED.displayValue
      )
    } else {
      editorConfigOverride
    },
  )
    .first

  private fun Set<RuleProvider>.lint(
    text: String,
    wrappingStyle: WrappingStyle = WrappingStyle.MINIMUM_RAGGED,
    lineLength: Int = 50,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): List<LintError> = ktlintTestLint(
    text = text.trimIndent(),
    editorConfigOverride = editorConfigOverride
  )
}
