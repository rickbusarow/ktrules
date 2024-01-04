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
import org.junit.jupiter.api.Test

class NoLeadingBlankLinesRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { NoLeadingBlankLinesRule() }
  )

  @Test
  fun `package declaration`() {

    format(
      """
      |
      |
      |
      |package com.test
      |
      |class MyClass
      |
      """.trimMargin()
    ) {

      expectError(
        line = 1,
        col = 1,
        ruleId = NoLeadingBlankLinesRule.ID,
        detail = NoLeadingBlankLinesRule.ERROR_MESSAGE
      )

      output shouldBe """
        |package com.test
        |
        |class MyClass
      """.trimMargin()
    }
  }

  @Test
  fun `file annotation`() {

    format(
      """
      |
      |
      |
      |@file:Suppress("DEPRECATION")
      |
      |package com.test
      |
      |class MyClass
      |
      """.trimMargin()
    ) {
      expectError(
        line = 1,
        col = 1,
        ruleId = NoLeadingBlankLinesRule.ID,
        detail = NoLeadingBlankLinesRule.ERROR_MESSAGE
      )

      output shouldBe """
        |@file:Suppress("DEPRECATION")
        |
        |package com.test
        |
        |class MyClass
      """.trimMargin()
    }
  }

  @Test
  fun `imports with no package declaration`() {

    format(
      """
      |
      |
      |
      |import java.io.Serializable
      |
      |class MyClass : Serializable
      |
      """.trimMargin()
    ) {
      expectError(
        line = 1,
        col = 1,
        ruleId = NoLeadingBlankLinesRule.ID,
        detail = NoLeadingBlankLinesRule.ERROR_MESSAGE
      )

      output shouldBe """
        |import java.io.Serializable
        |
        |class MyClass : Serializable
      """.trimMargin()
    }
  }

  @Test
  fun `code with no imports or package declaration`() {

    format(
      """
      |
      |
      |
      |class MyClass
      |
      """.trimMargin()
    ) {
      expectError(
        line = 1,
        col = 1,
        ruleId = NoLeadingBlankLinesRule.ID,
        detail = NoLeadingBlankLinesRule.ERROR_MESSAGE
      )

      output shouldBe """
        |class MyClass
      """.trimMargin()
    }
  }

  @Test
  fun `file license header`() {

    format(
      """
      |
      |
      |
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |package com.test
      |
      |class MyClass
      |
      """.trimMargin()
    ) {
      expectError(
        line = 1,
        col = 1,
        ruleId = NoLeadingBlankLinesRule.ID,
        detail = NoLeadingBlankLinesRule.ERROR_MESSAGE
      )

      output shouldBe """
        |/*
        | * Copyright (C) 1985 Sylvester Stallone
        | */
        |
        |package com.test
        |
        |class MyClass
      """.trimMargin()
    }
  }
}
