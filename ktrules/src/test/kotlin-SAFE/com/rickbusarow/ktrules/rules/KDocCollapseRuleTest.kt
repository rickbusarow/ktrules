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

package com.rickbusarow.ktrules.rules

import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.rules.Tests.KtLintTestResult
import org.junit.jupiter.api.Test

class KDocCollapseRuleTest : Tests {

  override val ruleProviders: Set<RuleProviderCompat> = setOf(
    RuleProviderCompat { KDocCollapseRule() }
  )

  @Test
  fun `a default section with two lines of text is not collapsed`() {

    format(
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
    ) {

      output shouldBe """
      /**
       * line one
       * line two
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `the kdoc is not collapsed if its collapsed length is one char beyond the max length`() {

    format(
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
    ) {

      output shouldBe """
      /**
       * This comment is 62 characters long when it's collapsed.
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `the kdoc is collapsed if its collapsed length is exactly the max length`() {

    format(
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
    ) {
      expectError()

      output shouldBe """
      /** This comment is 62 characters long when it's collapsed. */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a properly collapsed default section kdoc does not emit`() {

    format(
      """
      /** short line */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      output shouldBe """
      /** short line */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `the kdoc is collapsed if its collapsed length is exactly one less than the max length`() {

    format(
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
    ) {
      expectError()

      output shouldBe """
      /** This comment is 62 characters long when it's collapsed. */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a default section 4-space-indent code block is collapsed but not trimmed`() {

    format(
      """
      /**
       *    paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /**    paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a default section with leading and trailing newlines is collapsed`() {

    format(
      """
      /**
       * paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a default section with a leading newline is collapsed`() {

    format(
      """
      /**
       * paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a default section with a trailing newline is collapsed`() {

    format(
      """
      /** paragraph one
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** paragraph one */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `an unknown tag with leading and trailing newlines is collapsed`() {

    format(
      """
      /**
       * @orange banana
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `an unknown tag with a leading newline is collapsed`() {

    format(
      """
      /**
       * @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `an unknown tag with a leading newline and no trailing space is collapsed`() {

    format(
      """
      /**
       * @orange banana*/
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `an unknown tag with a trailing newline is collapsed`() {

    format(
      """
      /** @orange banana
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @orange banana */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a known tag with leading and trailing newlines is collapsed`() {

    format(
      """
      /**
       * @property name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a known tag with a leading newline is collapsed`() {

    format(
      """
      /**
       * @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a known tag with a leading newline and no trailing space is collapsed`() {

    format(
      """
      /**
       * @property name*/
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a known tag with a trailing newline is collapsed`() {

    format(
      """
      /** @property name
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      expectError()

      output shouldBe """
      /** @property name */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  private fun KtLintTestResult.expectError(line: Int = 1, col: Int = 1) {
    expectError(
      line = line,
      col = col,
      ruleId = KDocCollapseRule.ID,
      detail = KDocCollapseRule.ERROR_MESSAGE
    )
  }
}
