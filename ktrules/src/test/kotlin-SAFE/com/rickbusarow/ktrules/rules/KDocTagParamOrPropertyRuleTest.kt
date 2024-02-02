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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class KDocTagParamOrPropertyRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { KDocTagParamOrPropertyRule() }
  )

  @Test
  fun `@param on a non-property parameter should not emit`() {

    lint(
      """
      /**
       * This is a test function
       * @param str a string parameter
       */
      fun test(str: String) {}
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `@property on a non-property parameter should be fixed`() {

    format(
      """
      /**
       * This is a test function
       * @property str a string parameter
       */
      fun test(str: String) {}
      """.trimIndent()
    ) {
      expectError(
        line = 3,
        col = 4,
        ruleId = KDocTagParamOrPropertyRule.ID,
        detail = "The KDoc tag '@property str' should use '@param'."
      )

      output shouldBe """
        /**
         * This is a test function
         * @param str a string parameter
         */
        fun test(str: String) {}
      """.trimIndent()
    }
  }

  @Test
  fun `@property on a property parameter should not emit`() {

    lint(
      """
      /**
       * This is a test class
       * @property name the name property
       */
      class Test(val name: String)
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `@param on a private property should not emit`() {

    lint(
      """
      /**
       * This is a test class
       * @param name the name property
       */
      class Test(private val name: String)
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `@property on a private property should be fixed`() {

    format(
      """
      /**
       * This is a test class
       * @property name the name property
       */
      class Test(private val name: String)
      """.trimIndent()
    ) {
      expectError(
        line = 3,
        col = 4,
        ruleId = KDocTagParamOrPropertyRule.ID,
        detail = "The KDoc tag '@property name' should use '@param'."
      )

      output shouldBe """
        /**
         * This is a test class
         * @param name the name property
         */
        class Test(private val name: String)
      """.trimIndent()
    }
  }

  @TestFactory
  fun `@param on a non-private property parameter should be fixed`() =
    listOf(
      "public",
      "internal",
      "protected"
    ).test({ it }) { visibility ->

      format(
        """
        /**
         * This is a test class
         * @param name the name property
         */
        open class Test($visibility val name: String)
        """.trimIndent()
      ) {

        expectError(
          line = 3,
          col = 4,
          ruleId = KDocTagParamOrPropertyRule.ID,
          detail = "The KDoc tag '@param name' should use '@property'."
        )

        output shouldBe """
          /**
           * This is a test class
           * @property name the name property
           */
          open class Test($visibility val name: String)
        """.trimIndent()
      }
    }
}
