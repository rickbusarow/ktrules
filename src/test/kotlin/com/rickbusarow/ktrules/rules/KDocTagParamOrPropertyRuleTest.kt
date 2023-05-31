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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KDocTagParamOrPropertyRuleTest : Tests {

  override val rules = setOf(
    RuleProviderCompat { KDocTagParamOrPropertyRule() }
  )

  @Test
  fun `@param on a non-property parameter should not emit`() {

    rules.lint(
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

    rules.format(
      """
      /**
       * This is a test function
       * @property str a string parameter
       */
      fun test(str: String) {}
      """.trimIndent()
    ) shouldBe """
      /**
       * This is a test function
       * @param str a string parameter
       */
      fun test(str: String) {}
    """.trimIndent()
  }

  @Test
  fun `@property on a property parameter should not emit`() {

    rules.lint(
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
  fun `@param on a property parameter should be fixed`() {

    rules.format(
      """
      /**
       * This is a test class
       * @param name the name property
       */
      class Test(val name: String)
      """.trimIndent()
    ) shouldBe """
      /**
       * This is a test class
       * @property name the name property
       */
      class Test(val name: String)
    """.trimIndent()
  }
}
