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

class NoSinceInKDocRuleTest : Tests {

  override val currentVersionDefault = "0.2.3"

  override val ruleProviders = setOf(
    RuleProviderCompat { NoSinceInKDocRule() }
  )

  @Test
  fun `a missing since tag is not added if the version is not defined in editorconfig`() {

    format(
      """
      /** a comment `fun foo()` */
      data class Subject(
        val name: String,
        val age: Int
      )
      """,
      currentVersion = null
    ) {
      expectNoErrors()

      output shouldBe """
        /** a comment `fun foo()` */
        data class Subject(
          val name: String,
          val age: Int
        )
      """.trimIndent()
    }
  }

  @Test
  fun `updated kdoc is indented properly for a top-level val after a package declaration`() {

    format(
      """
      package builds

      /** a comment */
      const val myProperty = ""
      """
    ) {
      expectError(3, 15)

      output shouldBe """
      |package builds
      |
      |/**
      | * a comment
      | *
      | * @since 0.2.3
      | */
      |const val myProperty = ""
      """.trimMargin()
    }
  }

  @Test
  fun `a collapsed default section paragraph has the since tag added`() {

    format(
      """
      /** a comment `fun foo()` */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {
      expectError(1, 27)

      output shouldBe """
        /**
         * a comment `fun foo()`
         *
         * @since 0.2.3
         */
        data class Subject(
          val name: String,
          val age: Int
        )
      """.trimIndent()
    }
  }

  @Test
  fun `a missing since does not emit if the version is a -SNAPSHOT`() {

    format(
      """
      /**
       * comment
       *
       * @property name a name
       */
      data class Subject(
        val name: String
      )
      """.trimIndent(),
      currentVersion = "0.0.1-SNAPSHOT"
    ) {

      output shouldBe
        """
      /**
       * comment
       *
       * @property name a name
       */
      data class Subject(
        val name: String
      )
        """.trimIndent()
    }
  }

  @Test
  fun `missing since in comment is auto-corrected`() {

    format(
      """
      /**
       * comment
       *
       * @property name a name
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 5, col = 2)

      output shouldBe """
      /**
       * comment
       *
       * @property name a name
       * @since $currentVersionDefault
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `missing since in empty comment is auto-corrected`() {

    format(
      """
      /** */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 1, col = 5)

      output shouldBe """
    /** @since $currentVersionDefault */
    data class Subject(
      val name: String
    )
      """.trimIndent()
    }
  }

  @Test
  fun `missing since in suppressed comment is auto-corrected`() {

    format(
      """
      /** @suppress */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 1, col = 15)

      output shouldBe """
    /**
     * @suppress
     * @since $currentVersionDefault
     */
    data class Subject(
      val name: String
    )
      """.trimIndent()
    }
  }

  @Test
  fun `missing since in nested comment is auto-corrected`() {

    format(
      """
      class Outer {
        /**
         * comment
         *
         * @property name a name
         */
        data class Subject(
          val name: String
        )
      }
      """.trimIndent()
    ) {
      expectError(line = 6, col = 4)

      output shouldBe """
      class Outer {
        /**
         * comment
         *
         * @property name a name
         * @since $currentVersionDefault
         */
        data class Subject(
          val name: String
        )
      }
      """.trimIndent()
    }
  }

  @Test
  fun `single-line kdoc is auto-corrected`() {

    format(
      """
      /** comment */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 1, col = 13)

      output shouldBe """
      /**
       * comment
       *
       * @since $currentVersionDefault
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `single-line kdoc with tag is auto-corrected`() {

    format(
      """
      /** @property name a name */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 1, col = 27)

      output shouldBe """
      /**
       * @property name a name
       * @since $currentVersionDefault
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `since tag without version content is auto-corrected`() {

    format(
      """
      /**
       * comment
       *
       * @property name a name
       * @since
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 6, col = 2, detail = "add '0.2.3' to `@since` tag")

      output shouldBe """
      /**
       * comment
       *
       * @property name a name
       * @since $currentVersionDefault
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `multi line kdoc without tags has blank line before since tag`() {

    format(
      """
      /**
       * comment
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 3, col = 2)

      output shouldBe """
      /**
       * comment
       *
       * @since $currentVersionDefault
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `multi line blank kdoc is auto-corrected`() {

    format(
      """
      /**
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {
      expectError(line = 2, col = 2)

      output shouldBe """
      /** @since $currentVersionDefault */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a missing since does not emit if the version is an -RC`() {

    format(
      """
      /**
       * comment
       *
       * @property name a name
       */
      data class Subject(
        val name: String
      )
      """.trimIndent(),
      currentVersion = "0.0.1-RC"
    ) {

      output shouldBe
        """
        /**
         * comment
         *
         * @property name a name
         */
        data class Subject(
          val name: String
        )
        """.trimIndent()
    }
  }

  @Test
  fun `existing since does not emit`() {

    format(
      """
      /**
       * comment
       *
       * @property name a name
       * @since 0.0.1
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    ) {

      output shouldBe """
      /**
       * comment
       *
       * @property name a name
       * @since 0.0.1
       */
      data class Subject(
        val name: String
      )
      """.trimIndent()
    }
  }

  private fun KtLintTestResult.expectError(
    line: Int,
    col: Int,
    detail: String = "add `@since $currentVersionDefault` to kdoc"
  ) {
    expectError(
      line = line,
      col = col,
      ruleId = NoSinceInKDocRule.ID,
      detail = detail
    )
  }
}
