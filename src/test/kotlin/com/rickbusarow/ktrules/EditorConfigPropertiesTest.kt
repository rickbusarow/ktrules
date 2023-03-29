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

package com.rickbusarow.ktrules

import com.rickbusarow.ktrules.rules.ALL_PROPERTIES
import com.rickbusarow.ktrules.rules.RULES_PREFIX
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditorConfigPropertiesTest {

  class Environment {

    val systemProp: String? by lazy { System.setProperty("ktrules.project_version", "0.0.1") }

    val ruleProviders by lazy {
      systemProp
      KtRulesRuleSetProvider().getRuleProviders()
    }

    val ids by lazy {
      ruleProviders.map { it.createNewRuleInstance().id }
        .plus(ALL_PROPERTIES.map { it.name.removePrefix("${RULES_PREFIX}_") })
    }

    @Suppress("EditorConfigEmptySection")
    val defaultConfig =
      //language=editorconfig
      """
      # KtLint specific settings
      # noinspection EditorConfigKeyCorrectness
      [{*.kt,*.kts}]
      ktlint_kt-rules_kdoc-leading-asterisk = enabled
      ktlint_kt-rules_kdoc-wrapping = enabled
      ktlint_kt-rules_no-duplicate-copyright-header = enabled
      ktlint_kt-rules_no-leading-blank-lines = enabled
      ktlint_kt-rules_no-since-in-kdoc = enabled
      ktlint_kt-rules_no-space-in-annotation-with-target = enabled
      ktlint_kt-rules_no-trailing-space-in-raw-string-literal = enabled
      ktlint_kt-rules_no-useless-constructor-keyword = enabled

      ktlint_kt-rules_project_version = 1.0.0
      ktlint_kt-rules_wrapping_style = equal

      [{*.kt,*.kts}]
      # actual kotlin settings go here
      """.trimIndent()
  }

  @Test
  fun `defaultConfig property matches all defined rule IDs`() = test {

    val ruleReg = """ktlint_kt-rules_(.*?) ?=.*""".toRegex()

    defaultConfig
      .lines()
      .mapNotNull { ruleReg.find(it)?.destructured?.component1() }
      .sorted() shouldBe ids
  }

  fun test(action: Environment.() -> Unit) {

    Environment().action()
  }
}
