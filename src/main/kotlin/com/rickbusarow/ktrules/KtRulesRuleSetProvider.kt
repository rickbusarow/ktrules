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

import com.rickbusarow.ktrules.compat.RuleProviderCompat
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

/** @since 1.1.1 */
object KtRulesRuleSetProvider {

  /**
   * Used in RuleSetProvider's `about` param in 0.47-0.48, and used in a Rule's `about` property in
   * 0.49+.
   *
   * @since 1.1.1
   */
  object About {

    /** @since 1.1.1 */
    const val MAINTAINER: String = "Rick Busarow"

    /** @since 1.1.1 */
    const val DESCRIPTION: String = "miscellaneous Ktlint rules"

    /** @since 1.1.1 */
    const val LICENSE: String = "Apache 2.0"

    /** @since 1.1.1 */
    const val REPOSITORY_URL: String = "https://www.github.com/rbusarow/ktrules"

    /** @since 1.1.1 */
    const val ISSUE_TRACKER_URL: String = "https://www.github.com/rbusarow/ktrules/issues"
  }

  /** @since 1.1.1 */
  const val ID: String = "kt-rules"

  /** @since 1.1.1 */
  fun getRuleProviders(): Set<RuleProviderCompat> = setOf(
    RuleProviderCompat { KDocBlankLinesRule() },
    RuleProviderCompat { KDocCollapseRule() },
    RuleProviderCompat { KDocContentWrappingRule() },
    RuleProviderCompat { KDocIndentAfterLeadingAsteriskRule() },
    RuleProviderCompat { KDocLeadingAsteriskRule() },
    RuleProviderCompat { KDocTagOrderRule() },
    RuleProviderCompat { KDocTagParamOrPropertyRule() },
    RuleProviderCompat { NoDuplicateCopyrightHeaderRule() },
    RuleProviderCompat { NoLeadingBlankLinesRule() },
    RuleProviderCompat { NoSinceInKDocRule() },
    RuleProviderCompat { NoSpaceInTargetedAnnotationRule() },
    RuleProviderCompat { NoTrailingSpacesInRawStringLiteralRule() },
    RuleProviderCompat { NoUselessConstructorKeywordRule() },
    RuleProviderCompat { NoWithTypeWithLambdaRule() }
  )
}
