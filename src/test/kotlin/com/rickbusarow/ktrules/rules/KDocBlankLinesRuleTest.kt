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

import com.rickbusarow.ktrules.compat.RuleProvider
import com.rickbusarow.ktrules.rules.Tests.KtLintResults
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KDocBlankLinesRuleTest : Tests {

  override val rules = setOf(
    RuleProvider { KDocBlankLinesRule() }
  )

  @Test
  fun `a single blank line between paragraphs is ignored`() {

    lint(
      """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `a second blank line in between paragraphs is removed`() {

    format(
      """
      /**
       * paragraph one
       *
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 2, "consecutive blank lines in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `two extra blank lines between paragraphs are both removed`() {

    format(
      """
      /**
       * paragraph one
       *
       *
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 2, "consecutive blank lines in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a leading blank line is removed`() {

    format(
      """
      /**
       *
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(2, 2, "leading blank line in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `two leading blank lines are both removed`() {

    format(
      """
      /**
       *
       *
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(2, 2, "leading blank line in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a trailing blank line is removed`() {

    format(
      """
      /**
       * paragraph one
       *
       * paragraph two
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(5, 2, "trailing blank line in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `two trailing blank lines are both removed`() {

    format(
      """
      /**
       * paragraph one
       *
       * paragraph two
       *
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(5, 2, "trailing blank line in kdoc")

      output shouldBe """
      /**
       * paragraph one
       *
       * paragraph two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single blank line between unknown tags is removed`() {

    format(
      """
      /**
       * paragraph one
       *
       * @orange banana potato
       *
       * @carrot kiwi strawberry
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(5, 2, "extra blank line before subsequent kdoc tag")

      output shouldBe """
      /**
       * paragraph one
       *
       * @orange banana potato
       * @carrot kiwi strawberry
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single blank line between known tags is removed`() {

    format(
      """
      /**
       * paragraph one
       *
       * @property name a name
       *
       * @property age the age
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(5, 2, "extra blank line before subsequent kdoc tag")

      output shouldBe """
      /**
       * paragraph one
       *
       * @property name a name
       * @property age the age
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a paragraph break in a tag is left alone`() {

    lint(
      """
      /**
       * paragraph one
       *
       * @property name a name
       *
       *   a paragraph about name
       * @property age the age
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }

  private fun KtLintResults.expectError(
    line: Int,
    col: Int,
    detail: String
  ) {
    expectError(
      line = line,
      col = col,
      ruleId = KDocBlankLinesRule.ID,
      detail = detail
    )
  }
}
