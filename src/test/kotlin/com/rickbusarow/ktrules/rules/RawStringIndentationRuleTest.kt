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

class RawStringIndentationRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { RawStringIndentationRule() }
  )

  @Test
  fun `@param on a non-property parameter should not emit`() {

    lint(
      """


      val raw =
       ${RawStringIndentationRule.TRIPLE_QUOTES}
        line one
        line two
           ${RawStringIndentationRule.TRIPLE_QUOTES}
      """
    ) shouldBe emptyList()
  }
}
