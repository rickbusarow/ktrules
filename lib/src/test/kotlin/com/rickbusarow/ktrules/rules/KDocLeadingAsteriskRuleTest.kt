/*
 * Copyright (C) 2025 Rick Busarow
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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KDocLeadingAsteriskRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { KDocLeadingAsteriskRule() }
  )

  @Test
  fun `asterisks are added to the default section`() {

    format(
      """
      /**
       extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat



       @property name the name property
       @property age a number, probably



       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(2, 2)
      expectError(7, 4)
      expectError(8, 12)
      expectError(13, 10)

      output shouldBe """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
       *
       *
       *
       * @property name the name property
       * @property age a number, probably
       *
       *
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single blank line after a tag and before kdoc end is fixed`() {

    format(
      """
      /**
       * @property age a number, probably

       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 2)

      output shouldBe """
      /**
       * @property age a number, probably
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single blank line before kdoc end is fixed`() {

    format(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea

       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 2)

      output shouldBe """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a single blank line between tags is fixed`() {

    format(
      """
      /**
       * @property name the name property

       * @property age a number, probably
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(4, 2)

      output shouldBe """
      /**
       * @property name the name property
       *
       * @property age a number, probably
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
  fun `a kdoc with all its asterisks is left alone`() {

    lint(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut  laboris nisteghi ut liquip ex ea
       * fugiat nulla para tur. Excepteur sipteur sint
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `a kdoc with all its asterisks and tags is left alone`() {

    lint(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
       *
       *
       *
       * @property name the name property
       * @property age a number, probably
       *
       *
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }

  private fun KtLintTestResult.expectError(line: Int, col: Int) {
    expectError(
      line = line,
      col = col,
      ruleId = KDocLeadingAsteriskRule.ID,
      detail = KDocLeadingAsteriskRule.ERROR_MESSAGE
    )
  }
}
