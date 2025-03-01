/*
 * Copyright (C) 2025 Rick Busarow
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

@file:Suppress("DEPRECATION")

package com.rickbusarow.ktrules

import com.google.auto.service.AutoService
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

@Suppress("ktlint:standard:class-naming", "ClassNaming")
@AutoService(RuleSetProvider::class)
class KtRulesRuleSetProviderV1_47 : RuleSetProvider {

  @Suppress("DeprecatedCallableAddReplaceWith")
  @Deprecated("Marked for removal in KtLint 0.48. See changelog or KDoc for more information.")
  override fun get(): RuleSet {
    return RuleSet(
      KtRulesRuleSetProvider.ID,
      *KtRulesRuleSetProvider.getRuleProviders()
        .map { RuleCompat47(it.createNewRuleInstance()) }
        .toTypedArray()
    )
  }
}
