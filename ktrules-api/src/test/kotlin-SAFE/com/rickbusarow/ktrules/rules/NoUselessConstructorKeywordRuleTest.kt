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
import org.junit.jupiter.api.Test

class NoUselessConstructorKeywordRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { NoUselessConstructorKeywordRule() }
  )

  @Test
  fun `annotated constructor keyword is not removed`() {

    format(
      """
      |class MyClass @Inject constructor(
      |  val name: String
      |)
      |
      """.trimMargin()
    ) {

      output shouldBe
        """
        |class MyClass @Inject constructor(
        |  val name: String
        |)
        """.trimMargin()
    }
  }

  @Test
  fun `private constructor keyword is not removed`() {

    format(
      """
      |class MyClass private constructor(
      |  val name: String
      |)
      |
      """.trimMargin()
    ) {

      output shouldBe
        """
        |class MyClass private constructor(
        |  val name: String
        |)
        """.trimMargin()
    }
  }

  @Test
  fun `useless constructor keyword is removed`() {

    format(
      """
      |class MyClass constructor(
      |  val name: String
      |)
      |
      """.trimMargin()
    ) {

      expectError(
        line = 1,
        col = 15,
        ruleId = NoUselessConstructorKeywordRule.ID,
        detail = NoUselessConstructorKeywordRule.ERROR_MESSAGE
      )

      output shouldBe
        """
        |class MyClass(
        |  val name: String
        |)
        """.trimMargin()
    }
  }
}
