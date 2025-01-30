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
import com.rickbusarow.ktrules.rules.internal.noDots
import org.junit.jupiter.api.Test

class NoTrailingSpacesInRawStringLiteralRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { NoTrailingSpacesInRawStringLiteralRule() }
  )

  val triple = "\"\"\""

  @Test
  fun `trailing spaces in a string literal are removed`() {

    // Use the interpunct (·) so that:
    // (1) this rule doesn't clean up the whitespaces when running against the full project
    // (2) stuff's actually visible

    format(
      """
      |const val name: String = $triple
      |·········
      |··fun·foo()·=·Unit···
      |·
      |$triple.trimIndent()
      |
      """.trimMargin().noDots
    ) {

      expectError(
        line = 2,
        col = 1,
        ruleId = NoTrailingSpacesInRawStringLiteralRule.ID,
        detail = NoTrailingSpacesInRawStringLiteralRule.ERROR_MESSAGE
      )
      expectError(
        line = 3,
        col = 10,
        ruleId = NoTrailingSpacesInRawStringLiteralRule.ID,
        detail = NoTrailingSpacesInRawStringLiteralRule.ERROR_MESSAGE
      )
      expectError(
        line = 3,
        col = 11,
        ruleId = NoTrailingSpacesInRawStringLiteralRule.ID,
        detail = NoTrailingSpacesInRawStringLiteralRule.ERROR_MESSAGE
      )

      output shouldBe
        """
        |const val name: String = $triple
        |
        |··fun·foo()·=·Unit
        |
        |$triple.trimIndent()
        """.trimMargin()
          .noDots
    }
  }
}
