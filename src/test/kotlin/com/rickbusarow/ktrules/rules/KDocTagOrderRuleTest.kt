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
import org.junit.jupiter.api.Test

class KDocTagOrderRuleTest : Tests {

  val rules = setOf(
    RuleProvider { KDocTagOrderRule() }
  )

  @Test
  fun `class tag sorting`() {

    rules.format(
      """
      /**
       * @property name the name property
       * @property age a number, probably
       * @throws IllegalStateException because
       * @see Other
       * @param R r type
       * @param T t type
       */
      class Subject <T, R>(
        val age: Int,
        val name: String,
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @param T t type
       * @param R r type
       * @property age a number, probably
       * @property name the name property
       * @see Other
       * @throws IllegalStateException because
       */
      class Subject <T, R>(
        val age: Int,
        val name: String,
      )
    """.trimIndent()
  }

  @Test
  fun `function tag sorting`() {

    rules.format(
      """
      interface Subject {

        /**
         * @sample foo
         * @param age a number, probably
         * @param name the name property
         * @throws IllegalStateException because
         * @param R r type
         * @see Other
         * @param T t type
         */
        fun <T, R> foo(name: String, age: Int)
      }
      """.trimIndent()
    ) shouldBe """
      interface Subject {

        /**
         * @param T t type
         * @param R r type
         * @param name the name property
         * @param age a number, probably
         * @sample foo
         * @see Other
         * @throws IllegalStateException because
         */
        fun <T, R> foo(name: String, age: Int)
      }
    """.trimIndent()
  }
}
