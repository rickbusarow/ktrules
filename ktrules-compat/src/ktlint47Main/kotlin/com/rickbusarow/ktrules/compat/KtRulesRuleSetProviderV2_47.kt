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

package com.rickbusarow.ktrules.compat

import com.google.auto.service.AutoService
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import com.rickbusarow.ktrules.KtRulesRuleSetProvider
import com.rickbusarow.ktrules.rules.internal.mapToSet

@AutoService(RuleSetProviderV2::class)
class KtRulesRuleSetProviderV2_47 : RuleSetProviderV2(
  KtRulesRuleSetProvider.ID,
  About(
    maintainer = KtRulesRuleSetProvider.About.maintainer,
    description = KtRulesRuleSetProvider.About.description,
    license = KtRulesRuleSetProvider.About.license,
    repositoryUrl = KtRulesRuleSetProvider.About.repositoryUrl,
    issueTrackerUrl = KtRulesRuleSetProvider.About.issueTrackerUrl,
  )
) {

  override fun getRuleProviders(): Set<RuleProvider> {

    return KtRulesRuleSetProvider.getRuleProviders()
      .mapToSet { shim ->
        RuleProvider { shim.createNewRuleInstance() }
      }
  }
}
