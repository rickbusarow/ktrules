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
import com.rickbusarow.ktrules.rules.Tests.KtLintResults
import org.junit.jupiter.api.Test

class NoWithTypeWithLambdaRuleTest : Tests {

  override val rules = setOf(
    RuleProvider { NoWithTypeWithLambdaRule() }
  )

  @Test
  fun `a type parameter withType without a lambda does not emit`() {

    format(
      """
      val foo = tasks.withType<String>()
      """.trimMargin()
    ) {
      expectNoErrors()

      output shouldBe
        """
        val foo = tasks.withType<String>()
        """.trimIndent()
    }
  }

  @Test
  fun `a type parameter withType without a lambda parameter name is fixed`() {

    format(
      """
      val foo = tasks.withType<String> {
        println("hello world")
      }
      """.trimMargin()
    ) {

      expectError(1, 17)

      output shouldBe
        """
        val foo = tasks.withType<String>().configureEach {
          println("hello world")
        }
        """.trimIndent()
    }
  }

  @Test
  fun `a type parameter withType with a lambda parameter name is fixed`() {

    format(
      """
      val foo = tasks.withType<String> { task ->
        println(task)
      }
      """.trimMargin()
    ) {

      expectError(1, 17)

      output shouldBe
        """
        val foo = tasks.withType<String>().configureEach { task ->
          println(task)
        }
        """.trimIndent()
    }
  }

  @Test
  fun `a class parameter withType without a lambda does not emit`() {

    format(
      """
      val foo = tasks.withType(String::class.java)
      """.trimMargin()
    ) {
      expectNoErrors()

      output shouldBe
        """
        val foo = tasks.withType(String::class.java)
        """.trimIndent()
    }
  }

  @Test
  fun `a class parameter withType without a lambda parameter name is fixed`() {

    format(
      """
      val foo = tasks.withType(String::class.java) {
        println("hello world")
      }
      """.trimMargin()
    ) {

      expectError(1, 17)

      output shouldBe
        """
        val foo = tasks.withType(String::class.java).configureEach {
          println("hello world")
        }
        """.trimIndent()
    }
  }

  @Test
  fun `a class parameter withType with a lambda parameter name is fixed`() {

    format(
      """
      val foo = tasks.withType(String::class.java) { task ->
        println(task)
      }
      """.trimMargin()
    ) {

      expectError(1, 17)

      output shouldBe
        """
        val foo = tasks.withType(String::class.java).configureEach { task ->
          println(task)
        }
        """.trimIndent()
    }
  }

  private fun KtLintResults.expectError(line: Int, col: Int) {
    expectError(
      line = line,
      col = col,
      ruleId = NoWithTypeWithLambdaRule.ID,
      detail = NoWithTypeWithLambdaRule.ERROR_MESSAGE
    )
  }
}
