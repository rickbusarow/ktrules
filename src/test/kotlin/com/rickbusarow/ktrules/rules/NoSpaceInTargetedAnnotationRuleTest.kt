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

class NoSpaceInTargetedAnnotationRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { NoSpaceInTargetedAnnotationRule() }
  )

  @Test
  fun `space after colon`() {

    format(
      """
      |@file: Suppress("DEPRECATION")
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
        ruleId = NoSpaceInTargetedAnnotationRule.ID,
        detail = NoSpaceInTargetedAnnotationRule.ERROR_MESSAGE
      )

      output shouldBe
        """
        |@file:Suppress("DEPRECATION")
        |
        |package com.test
        |
        |class MyClass
        """.trimMargin()
    }
  }

  @Test
  fun `space before colon`() {

    format(
      """
      |@file :Suppress("DEPRECATION")
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
        ruleId = NoSpaceInTargetedAnnotationRule.ID,
        detail = NoSpaceInTargetedAnnotationRule.ERROR_MESSAGE
      )

      output shouldBe
        """
        |@file:Suppress("DEPRECATION")
        |
        |package com.test
        |
        |class MyClass
        """.trimMargin()
    }
  }

  @Test
  fun `parameter list spaces are left alone`() {

    format(
      """
      |@file:Suppress(
      |  "DEPRECATION"
      |)
      |
      |package com.test
      |
      |class MyClass
      |
      """.trimMargin()
    ) {

      output shouldBe
        """
        |@file:Suppress(
        |  "DEPRECATION"
        |)
        |
        |package com.test
        |
        |class MyClass
        """.trimMargin()
    }
  }
}
