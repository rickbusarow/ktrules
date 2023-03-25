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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KtRulesRuleSetProviderTest {

  class Environment {

    val systemProp: String? by lazy { System.setProperty("ktrules.current_version", "0.0.1") }

    val ruleProviders by lazy {
      systemProp
      KtRulesRuleSetProvider().getRuleProviders()
    }

    val ids by lazy { ruleProviders.map { it.createNewRuleInstance().id } }

    val defaultConfig =
      //language=editorconfig
      """
      # KtLint specific settings
      # noinspection EditorConfigKeyCorrectness
      [{*.kt,*.kts}]
      kt-rules_kdoc-leading-asterisk = enabled
      kt-rules_kdoc-wrapping = enabled
      kt-rules_no-duplicate-copyright-header = enabled
      kt-rules_no-leading-blank-lines = enabled
      kt-rules_no-since-in-kdoc = enabled
      kt-rules_no-space-in-annotation-with-target = enabled
      kt-rules_no-trailing-space-in-raw-string-literal = enabled
      kt-rules_no-useless-constructor-keyword = enabled

      [{*.kt,*.kts}]
      # actual kotlin settings go here
      """.trimIndent()
  }

  @Test
  fun `defaultConfig property matches all defined rule IDs`() = test {

    val ruleReg = """kt-rules_(.*?) ?=.*""".toRegex()

    defaultConfig
      .lines()
      .mapNotNull { ruleReg.find(it)?.destructured?.component1() }
      .sorted() shouldBe ids
  }

  fun test(action: Environment.() -> Unit) {
    Environment().action()
  }
}
