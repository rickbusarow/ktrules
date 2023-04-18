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

import com.pinterest.ktlint.core.RuleProvider
import org.junit.jupiter.api.TestFactory

class NoDuplicateCopyrightHeaderRuleTest : Tests {

  override val rules = setOf(
    RuleProvider { NoDuplicateCopyrightHeaderRule() }
  )

  @TestFactory
  fun `consecutive duplicate file license header is removed`() = listOf(
    "script" to "foo.kts",
    "normal" to "foo.kt"
  ).test({ it.first }) { (_, filePath) ->

    rules.format(
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |package com.test
      |
      |class MyClass
      |
      """.trimMargin(),
      filePath = filePath
    ) shouldBe
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |package com.test
      |
      |class MyClass
      """.trimMargin()
  }

  @TestFactory
  fun `duplicate file license header after package declaration is removed`() = listOf(
    "script" to "foo.kts",
    "normal" to "foo.kt"
  ).test({ it.first }) { (_, filePath) ->

    rules.format(
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |package com.test
      |
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |class MyClass
      |
      """.trimMargin(),
      filePath = filePath
    ) shouldBe
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |package com.test
      |
      |class MyClass
      """.trimMargin()
  }

  @TestFactory
  fun `duplicate file license header after import is removed`() = listOf(
    "script" to "foo.kts",
    "normal" to "foo.kt"
  ).test({ it.first }) { (_, filePath) ->

    rules.format(
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |import java.io.Serializable
      |
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |class MyClass : Serializable
      |
      """.trimMargin(),
      filePath = filePath
    ) shouldBe
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |import java.io.Serializable
      |
      |class MyClass : Serializable
      """.trimMargin()
  }

  @TestFactory
  fun `duplicate file license header after file annotation is removed`() = listOf(
    "script" to "foo.kts",
    "normal" to "foo.kt"
  ).test({ it.first }) { (_, filePath) ->

    rules.format(
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |@file:Suppress("MAGIC_NUMBER")
      |
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |class MyClass
      |
      """.trimMargin(),
      filePath = filePath
    ) shouldBe
      """
      |/*
      | * Copyright (C) 1985 Sylvester Stallone
      | */
      |
      |@file:Suppress("MAGIC_NUMBER")
      |
      |class MyClass
      """.trimMargin()
  }
}
