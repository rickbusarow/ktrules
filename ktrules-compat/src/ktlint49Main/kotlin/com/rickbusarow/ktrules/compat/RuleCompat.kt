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

import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.rickbusarow.ktrules.KtRulesRuleSetProvider
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAsLateAsPossibleCompat

actual abstract class RuleCompat actual constructor(
  ruleId: RuleId,
  visitorModifiers: Set<VisitorModifierCompat>,
  usesEditorConfigProperties: Set<EditorConfigProperty<*>>
) : Rule(
  com.pinterest.ktlint.rule.engine.core.api.RuleId(
    "${KtRulesRuleSetProvider.ID}:${ruleId.value}"
  ),
  About(
    maintainer = KtRulesRuleSetProvider.About.maintainer,
    repositoryUrl = KtRulesRuleSetProvider.About.repositoryUrl,
    issueTrackerUrl = KtRulesRuleSetProvider.About.issueTrackerUrl,
  ),
  visitorModifiers = visitorModifiers.mapToSet { visitorModifier ->

    when (visitorModifier) {
      is RunAfterRuleCompat -> VisitorModifier.RunAfterRule(
        com.pinterest.ktlint.rule.engine.core.api.RuleId(
          "${KtRulesRuleSetProvider.ID}:${visitorModifier.ruleId.value}"
        ),
        when (visitorModifier.mode) {
          REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED ->
            VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED

          ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED ->
            VisitorModifier.RunAfterRule.Mode.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
        }
      )

      RunAsLateAsPossibleCompat -> VisitorModifier.RunAsLateAsPossible
    }
  },
  usesEditorConfigProperties = usesEditorConfigProperties.mapToSet { shimProperty ->
    shimProperty.toKtLintProperty49()
  }
) {

  override fun beforeFirstNode(editorConfig: EditorConfig) {
    beforeFirstNode(EditorConfigCompat49(editorConfig))
    super.beforeFirstNode(editorConfig)
  }

  actual open fun beforeFirstNode(editorConfigProperties: EditorConfigCompat) = Unit
}
