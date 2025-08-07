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

import com.rickbusarow.ktrules.KtRulesRuleSetProvider
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import org.junit.jupiter.api.Test

class KDocRuleIntegrationTest : Tests {

  override val ruleProviders: Set<RuleProviderCompat> = KtRulesRuleSetProvider.getRuleProviders()

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

      expectError(4, 2, NoSinceInKDocRule.ID, "add `@since 0.2.3` to kdoc")
      expectError(2, 2, KDocContentWrappingRule.ID, "kdoc content wrapping")

      output shouldBe """
      /**
       * line one line two
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
  fun `super canary`() {

    format(
      """
      /**
       * @property name foo
       * @since 0.1.2
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """
    ) {

      output shouldBe """
      /**
       * @property name foo
       * @since 0.1.2
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
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

      expectError(1, 27, NoSinceInKDocRule.ID, "add `@since 0.2.3` to kdoc")

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

      expectError(3, 2, NoSinceInKDocRule.ID, "add `@since 0.2.3` to kdoc")

      output shouldBe """
      /**
       * This comment is 62 characters long when it's collapsed.
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
}
