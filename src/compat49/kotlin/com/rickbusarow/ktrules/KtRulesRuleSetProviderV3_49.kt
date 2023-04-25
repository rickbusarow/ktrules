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

import com.google.auto.service.AutoService
import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.rules.internal.mapToSet

@AutoService(RuleSetProviderV3::class)
class KtRulesRuleSetProviderV3_49 : RuleSetProviderV3(RuleSetId(KtRulesRuleSetProvider.ID)) {

  override fun getRuleProviders(): Set<RuleProvider> {

    return KtRulesRuleSetProvider.getRuleProviders()
      .mapToSet { it.toKtLintRuleProvider49() }
  }
}

/** */
fun RuleProviderCompat.toKtLintRuleProvider49(): RuleProvider {
  return RuleProvider { RuleCompat49(createNewRuleInstance()) }
}
