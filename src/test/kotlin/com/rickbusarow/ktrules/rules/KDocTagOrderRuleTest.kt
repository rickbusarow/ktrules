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

import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import org.junit.jupiter.api.Test

class KDocTagOrderRuleTest : Tests {

  override val rules = setOf(
    RuleProvider { KDocTagOrderRule() }
  )

  @Test
  fun `class tag sorting`() {

    rules.format(
      """
      /**
       * @property yellowWatermelon some comment
       * @property jackfruit some comment
       * @property quince some comment
       * @param raspberry some comment
       * @property banana some comment
       * @property mango some comment
       * @property cherry some comment
       * @param T t type
       * @property name the name property
       * @property watermelon some comment
       * @param orange some comment
       * @see Other
       * @property tangerine some comment
       * @property ugliFruit some comment
       * @property elderberry some comment
       * @property apple some comment
       * @property kiwi some comment
       * @property fig some comment
       * @throws IllegalStateException because
       * @property vanillaBean some comment
       * @property lemon some comment
       * @param pineapple some comment
       * @property grape some comment
       * @property age a number, probably
       * @property honeydew some comment
       * @param nectarine some comment
       * @param zucchini some comment
       * @param date some comment
       * @param R r type
       * @property imbe some comment
       * @property strawberry some comment
       * @property xigua some comment
       */
      class Subject <T, R>(
        val age: Int,
        val name: String,
        val honeydew : String,
        val elderberry : String,
        val yellowWatermelon : String,
        date : String,
        val grape : String,
        val mango : String,
        val tangerine : String,
        val fig : String,
        val kiwi : String,
        val xigua : String,
        orange : String,
        val banana : String,
        val cherry : String,
        val jackfruit : String,
        zucchini : String,
        val ugliFruit : String,
        val quince : String,
        nectarine : String,
        val watermelon : String,
        val apple : String,
        pineapple : String,
        val lemon : String,
        val strawberry : String,
        raspberry : String,
        val imbe : String,
        val vanillaBean : String,
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @param T t type
       * @param R r type
       * @property age a number, probably
       * @property name the name property
       * @property honeydew some comment
       * @property elderberry some comment
       * @property yellowWatermelon some comment
       * @param date some comment
       * @property grape some comment
       * @property mango some comment
       * @property tangerine some comment
       * @property fig some comment
       * @property kiwi some comment
       * @property xigua some comment
       * @param orange some comment
       * @property banana some comment
       * @property cherry some comment
       * @property jackfruit some comment
       * @param zucchini some comment
       * @property ugliFruit some comment
       * @property quince some comment
       * @param nectarine some comment
       * @property watermelon some comment
       * @property apple some comment
       * @param pineapple some comment
       * @property lemon some comment
       * @property strawberry some comment
       * @param raspberry some comment
       * @property imbe some comment
       * @property vanillaBean some comment
       * @see Other
       * @throws IllegalStateException because
       */
      class Subject <T, R>(
        val age: Int,
        val name: String,
        val honeydew : String,
        val elderberry : String,
        val yellowWatermelon : String,
        date : String,
        val grape : String,
        val mango : String,
        val tangerine : String,
        val fig : String,
        val kiwi : String,
        val xigua : String,
        orange : String,
        val banana : String,
        val cherry : String,
        val jackfruit : String,
        zucchini : String,
        val ugliFruit : String,
        val quince : String,
        nectarine : String,
        val watermelon : String,
        val apple : String,
        pineapple : String,
        val lemon : String,
        val strawberry : String,
        raspberry : String,
        val imbe : String,
        val vanillaBean : String,
      )
    """.trimIndent()
  }

  @Test
  fun `function tag sorting`() {

    rules.format(
      """
      interface Subject {
        /**
         * @param yellowWatermelon some comment
         * @param jackfruit some comment
         * @param quince some comment
         * @param raspberry some comment
         * @param banana some comment
         * @param mango some comment
         * @param cherry some comment
         * @param T t type
         * @param name the name property
         * @param watermelon some comment
         * @param orange some comment
         * @see Other
         * @param tangerine some comment
         * @param ugliFruit some comment
         * @param elderberry some comment
         * @param apple some comment
         * @param kiwi some comment
         * @param fig some comment
         * @throws IllegalStateException because
         * @param vanillaBean some comment
         * @param lemon some comment
         * @param pineapple some comment
         * @param grape some comment
         * @param age a number, probably
         * @param honeydew some comment
         * @param nectarine some comment
         * @param zucchini some comment
         * @param date some comment
         * @param R r type
         * @param imbe some comment
         * @param strawberry some comment
         * @param xigua some comment
         */
        fun <T, R> foo(
          age: Int,
          name: String,
          honeydew : String,
          elderberry : String,
          yellowWatermelon : String,
          date : String,
          grape : String,
          mango : String,
          tangerine : String,
          fig : String,
          kiwi : String,
          xigua : String,
          orange : String,
          banana : String,
          cherry : String,
          jackfruit : String,
          zucchini : String,
          ugliFruit : String,
          quince : String,
          nectarine : String,
          watermelon : String,
          apple : String,
          pineapple : String,
          lemon : String,
          strawberry : String,
          raspberry : String,
          imbe : String,
          vanillaBean : String,)
      }
      """.trimIndent()
    ) shouldBe """
      interface Subject {
        /**
         * @param T t type
         * @param R r type
         * @param age a number, probably
         * @param name the name property
         * @param honeydew some comment
         * @param elderberry some comment
         * @param yellowWatermelon some comment
         * @param date some comment
         * @param grape some comment
         * @param mango some comment
         * @param tangerine some comment
         * @param fig some comment
         * @param kiwi some comment
         * @param xigua some comment
         * @param orange some comment
         * @param banana some comment
         * @param cherry some comment
         * @param jackfruit some comment
         * @param zucchini some comment
         * @param ugliFruit some comment
         * @param quince some comment
         * @param nectarine some comment
         * @param watermelon some comment
         * @param apple some comment
         * @param pineapple some comment
         * @param lemon some comment
         * @param strawberry some comment
         * @param raspberry some comment
         * @param imbe some comment
         * @param vanillaBean some comment
         * @see Other
         * @throws IllegalStateException because
         */
        fun <T, R> foo(
          age: Int,
          name: String,
          honeydew : String,
          elderberry : String,
          yellowWatermelon : String,
          date : String,
          grape : String,
          mango : String,
          tangerine : String,
          fig : String,
          kiwi : String,
          xigua : String,
          orange : String,
          banana : String,
          cherry : String,
          jackfruit : String,
          zucchini : String,
          ugliFruit : String,
          quince : String,
          nectarine : String,
          watermelon : String,
          apple : String,
          pineapple : String,
          lemon : String,
          strawberry : String,
          raspberry : String,
          imbe : String,
          vanillaBean : String,)
      }
    """.trimIndent()
  }
}
