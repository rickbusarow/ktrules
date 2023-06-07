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
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.rules.internal.mapToSet

@Suppress("ktlint:standard:class-naming", "ClassNaming")
@AutoService(RuleSetProviderV2::class)
class KtRulesRuleSetProviderV2_48 : RuleSetProviderV2(
  KtRulesRuleSetProvider.ID,
  About(
    maintainer = KtRulesRuleSetProvider.About.MAINTAINER,
    description = KtRulesRuleSetProvider.About.DESCRIPTION,
    license = KtRulesRuleSetProvider.About.LICENSE,
    repositoryUrl = KtRulesRuleSetProvider.About.REPOSITORY_URL,
    issueTrackerUrl = KtRulesRuleSetProvider.About.ISSUE_TRACKER_URL
  )
) {

  override fun getRuleProviders(): Set<RuleProvider> {

    return KtRulesRuleSetProvider.getRuleProviders()
      .mapToSet { shim ->
        RuleProvider { RuleCompat48(shim.createNewRuleInstance()) }
      }
  }
}

/**  */
fun RuleProviderCompat.toKtLintRuleProvider48(): RuleProvider {
  return RuleProvider { RuleCompat48(createNewRuleInstance()) }
}
