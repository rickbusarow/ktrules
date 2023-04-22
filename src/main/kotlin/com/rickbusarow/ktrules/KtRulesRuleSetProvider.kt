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
import com.rickbusarow.ktrules.compat.RuleAbout
import com.rickbusarow.ktrules.compat.RuleProvider
import com.rickbusarow.ktrules.compat.RuleSetId
import com.rickbusarow.ktrules.compat.RuleSetProvider
import com.rickbusarow.ktrules.rules.KDocBlankLinesRule
import com.rickbusarow.ktrules.rules.KDocCollapseRule
import com.rickbusarow.ktrules.rules.KDocContentWrappingRule
import com.rickbusarow.ktrules.rules.KDocIndentAfterLeadingAsteriskRule
import com.rickbusarow.ktrules.rules.KDocLeadingAsteriskRule
import com.rickbusarow.ktrules.rules.KDocTagOrderRule
import com.rickbusarow.ktrules.rules.KDocTagParamOrPropertyRule
import com.rickbusarow.ktrules.rules.NoDuplicateCopyrightHeaderRule
import com.rickbusarow.ktrules.rules.NoLeadingBlankLinesRule
import com.rickbusarow.ktrules.rules.NoSinceInKDocRule
import com.rickbusarow.ktrules.rules.NoSpaceInTargetedAnnotationRule
import com.rickbusarow.ktrules.rules.NoTrailingSpacesInRawStringLiteralRule
import com.rickbusarow.ktrules.rules.NoUselessConstructorKeywordRule
import com.rickbusarow.ktrules.rules.NoWithTypeWithLambdaRule

@AutoService(RuleSetProvider::class)
class KtRulesRuleSetProvider : RuleSetProvider(
  id = RuleSetId("kt-rules")
) {

  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider { KDocBlankLinesRule() },
      RuleProvider { KDocCollapseRule() },
      RuleProvider { KDocContentWrappingRule() },
      RuleProvider { KDocIndentAfterLeadingAsteriskRule() },
      RuleProvider { KDocLeadingAsteriskRule() },
      RuleProvider { KDocTagOrderRule() },
      RuleProvider { KDocTagParamOrPropertyRule() },
      RuleProvider { NoDuplicateCopyrightHeaderRule() },
      RuleProvider { NoLeadingBlankLinesRule() },
      RuleProvider { NoSinceInKDocRule() },
      RuleProvider { NoSpaceInTargetedAnnotationRule() },
      RuleProvider { NoTrailingSpacesInRawStringLiteralRule() },
      RuleProvider { NoUselessConstructorKeywordRule() },
      RuleProvider { NoWithTypeWithLambdaRule() }
    )
  }

  companion object {
    /** @since 1.1.0 */
    val ABOUT: RuleAbout = RuleAbout(
      maintainer = "Rick Busarow",
      repositoryUrl = "https://www.github.com/rbusarow/ktrules",
      issueTrackerUrl = "https://www.github.com/rbusarow/ktrules/issues",
    )
  }
}
