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

@file:Suppress("stuff")

package com.rickbusarow.ktrules

import com.google.auto.service.AutoService
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import com.rickbusarow.ktrules.rules.KDocLeadingAsteriskRule
import com.rickbusarow.ktrules.rules.KDocWrappingRule
import com.rickbusarow.ktrules.rules.NoDuplicateCopyrightHeaderRule
import com.rickbusarow.ktrules.rules.NoLeadingBlankLinesRule
import com.rickbusarow.ktrules.rules.NoSinceInKDocRule
import com.rickbusarow.ktrules.rules.NoSpaceInTargetedAnnotationRule
import com.rickbusarow.ktrules.rules.NoTrailingSpacesInRawStringLiteralRule
import com.rickbusarow.ktrules.rules.NoUselessConstructorKeywordRule

@AutoService(RuleSetProviderV2::class)
class KtRulesRuleSetProvider : RuleSetProviderV2(
  id = "kt-rules",
  about = About(
    maintainer = "Rick Busarow",
    description = "miscellaneous Ktlint rules",
    license = "Apache 2.0",
    repositoryUrl = "https://www.github.com/rbusarow/ktrules",
    issueTrackerUrl = "https://www.github.com/rbusarow/ktrules/issues",
  )
) {

  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider { KDocLeadingAsteriskRule() },
      RuleProvider { KDocWrappingRule() },
      RuleProvider { NoDuplicateCopyrightHeaderRule() },
      RuleProvider { NoLeadingBlankLinesRule() },
      RuleProvider { NoSinceInKDocRule() },
      RuleProvider { NoSpaceInTargetedAnnotationRule() },
      RuleProvider { NoTrailingSpacesInRawStringLiteralRule() },
      RuleProvider { NoUselessConstructorKeywordRule() }
    )
  }
}
