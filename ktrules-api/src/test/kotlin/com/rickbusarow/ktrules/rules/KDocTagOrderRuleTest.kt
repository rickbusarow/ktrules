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

class KDocTagOrderRuleTest : Tests {

  override val ruleProviders = setOf(
    RuleProviderCompat { KDocTagOrderRule() }
  )

  @Test
  fun `class tag sorting`() {

    format(
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
    ) {

      val paramDetail = "KDoc tag order is incorrect. @param should be sorted."
      val propertyDetail = "KDoc tag order is incorrect. @property should be sorted."

      val seeDetail = "KDoc tag order is incorrect. @see should be sorted."
      val throwsDetail = "KDoc tag order is incorrect. @throws should be sorted."

      expectError(2, 4, KDocTagOrderRule.ID, propertyDetail)
      expectError(2, 23, KDocTagOrderRule.ID, propertyDetail)
      expectError(2, 42, KDocTagOrderRule.ID, propertyDetail)
      expectError(3, 35, KDocTagOrderRule.ID, paramDetail)
      expectError(5, 2, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 6, col = 4, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 7, col = 8, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 8, col = 19, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 9, col = 14, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 10, col = 27, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 11, col = 23, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 12, col = 22, KDocTagOrderRule.ID, seeDetail)
      expectError(line = 14, col = 8, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 15, col = 3, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 15, col = 35, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 16, col = 29, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 17, col = 25, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 18, col = 26, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 20, col = 1, KDocTagOrderRule.ID, throwsDetail)
      expectError(line = 20, col = 33, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 21, col = 28, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 22, col = 23, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 23, col = 24, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 24, col = 28, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 25, col = 28, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 26, col = 25, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 27, col = 22, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 28, col = 26, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 29, col = 27, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 31, col = 11, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 32, col = 18, KDocTagOrderRule.ID, propertyDetail)
      expectError(line = 32, col = 32, KDocTagOrderRule.ID, propertyDetail)

      output shouldBe """
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
  }

  @Test
  fun `function tag sorting`() {

    format(
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
          vanillaBean : String
        )
      }
      """.trimIndent()
    ) {

      val paramDetail = "KDoc tag order is incorrect. @param should be sorted."
      val seeDetail = "KDoc tag order is incorrect. @see should be sorted."
      val throwsDetail = "KDoc tag order is incorrect. @throws should be sorted."

      expectError(line = 3, col = 6, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 3, col = 27, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 4, col = 6, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 5, col = 6, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 6, col = 9, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 7, col = 8, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 8, col = 12, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 9, col = 23, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 10, col = 21, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 11, col = 31, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 12, col = 27, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 13, col = 26, KDocTagOrderRule.ID, seeDetail)
      expectError(line = 15, col = 7, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 16, col = 2, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 16, col = 33, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 17, col = 30, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 18, col = 26, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 19, col = 27, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 21, col = 3, KDocTagOrderRule.ID, throwsDetail)
      expectError(line = 21, col = 37, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 22, col = 29, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 23, col = 24, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 24, col = 28, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 25, col = 29, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 26, col = 29, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 27, col = 29, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 28, col = 26, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 29, col = 27, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 30, col = 28, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 32, col = 7, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 33, col = 14, KDocTagOrderRule.ID, paramDetail)
      expectError(line = 33, col = 30, KDocTagOrderRule.ID, paramDetail)

      output shouldBe """
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
            vanillaBean : String
          )
        }
      """.trimIndent()
    }
  }

  @Test
  fun `tags are sorted after adding a since tag`() {

    format(
      """
      interface Subject {
        /**
         * @param age a number, probably
         * @throws IllegalArgumentException because
         */
        fun <T, R> foo( age: Int)
      }
      """.trimIndent(),
      rules = setOf(
        RuleProviderCompat { KDocTagOrderRule() },
        RuleProviderCompat { NoSinceInKDocRule() }
      )
    ) {

      val throwsDetail = "KDoc tag order is incorrect. @throws should be sorted."
      val sinceDetail = "KDoc tag order is incorrect. @since should be sorted."

      expectError(line = 4, col = 6, KDocTagOrderRule.ID, throwsDetail)
      expectError(line = 4, col = 24, KDocTagOrderRule.ID, sinceDetail)
      expectError(line = 5, col = 4, NoSinceInKDocRule.ID, "add `@since 0.2.3` to kdoc")

      output shouldBe """
        interface Subject {
          /**
           * @param age a number, probably
           * @since 0.2.3
           * @throws IllegalArgumentException because
           */
          fun <T, R> foo( age: Int)
        }
      """.trimIndent()
    }
  }

  @Test
  fun `tags are sorted after switching a param to a property`() {

    format(
      """
      /**
       * @throws IllegalArgumentException because
       * @param age a number
       */
      class Subject(
        val age: Int
      )
      """.trimIndent(),
      rules = setOf(
        RuleProviderCompat { KDocTagOrderRule() },
        RuleProviderCompat { KDocTagParamOrPropertyRule() }
      )
    ) {

      val throwsDetail = "KDoc tag order is incorrect. @throws should be sorted."
      val propertyDetail = "KDoc tag order is incorrect. @property should be sorted."

      expectError(line = 2, col = 4, KDocTagOrderRule.ID, throwsDetail)
      expectError(line = 2, col = 30, KDocTagOrderRule.ID, propertyDetail)
      expectError(
        line = 3,
        col = 4,
        KDocTagParamOrPropertyRule.ID,
        "The KDoc tag '@param age' should use '@property'."
      )

      output shouldBe """
      /**
       * @property age a number
       * @throws IllegalArgumentException because
       */
      class Subject(
        val age: Int
      )
      """.trimIndent()
    }
  }
}
