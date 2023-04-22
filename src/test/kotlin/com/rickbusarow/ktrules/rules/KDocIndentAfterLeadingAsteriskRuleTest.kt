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
import org.junit.jupiter.api.Test

class KDocIndentAfterLeadingAsteriskRuleTest : Tests {

  override val rules = setOf(
    RuleProvider { KDocIndentAfterLeadingAsteriskRule() }
  )

  @Test
  fun `a space is added for a default section comment`() {

    format(
      """
      /**
       *comment
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
       * comment
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a space is added for a known tag`() {

    format(
      """
      /**
       *@property name desc
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
       * @property name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a space is added for an unknown tag`() {

    format(
      """
      /**
       *@banana name desc
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
       * @banana name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `three spaces are added after a tag`() {

    format(
      """
      /**
       * @property name desc
       *second line
       * third line
       *  fourth line
       *   fifth line
       *    sixth line
       *
       *second paragraph
       *
       * third paragraph
       *
       *  fourth paragraph
       *
       *   fifth paragraph
       *
       *    sixth paragraph
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(3, 2)
      expectError(4, 5)
      expectError(5, 7)
      expectError(9, 8)
      expectError(11, 11)
      expectError(13, 13)

      output shouldBe """
      /**
       * @property name desc
       *   second line
       *   third line
       *   fourth line
       *   fifth line
       *    sixth line
       *
       *   second paragraph
       *
       *   third paragraph
       *
       *   fourth paragraph
       *
       *   fifth paragraph
       *
       *    sixth paragraph
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  @Test
  fun `a space is added for a collapsed default section comment`() {

    format(
      """
      /**comment */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) {
      expectError(1, 1)

      output shouldBe """
      /** comment */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    }
  }

  private fun KtLintResults.expectError(line: Int, col: Int) {
    expectError(
      line = line,
      col = col,
      ruleId = KDocIndentAfterLeadingAsteriskRule.ID,
      detail = KDocIndentAfterLeadingAsteriskRule.ERROR_MESSAGE
    )
  }
}
