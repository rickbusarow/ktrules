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

import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAsLateAsPossibleCompat
import com.rickbusarow.ktrules.rules.internal.mapToSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/** */
class RuleCompat49(private val ruleCompat: RuleCompat) : Rule(
  RuleId("${KtRulesRuleSetProvider.ID}:${ruleCompat.ruleId.value}"),
  About(
    maintainer = KtRulesRuleSetProvider.About.maintainer,
    repositoryUrl = KtRulesRuleSetProvider.About.repositoryUrl,
    issueTrackerUrl = KtRulesRuleSetProvider.About.issueTrackerUrl,
  ),
  visitorModifiers = ruleCompat.visitorModifiers.mapToSet { visitorModifier ->

    when (visitorModifier) {
      is RunAfterRuleCompat -> VisitorModifier.RunAfterRule(
        RuleId("${KtRulesRuleSetProvider.ID}:${visitorModifier.ruleId.value}"),
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
  usesEditorConfigProperties = ruleCompat.usesEditorConfigProperties.mapToSet { shimProperty ->
    shimProperty.toKtLintProperty49()
  }
) {

  override fun beforeFirstNode(editorConfig: EditorConfig) {
    ruleCompat.beforeFirstNode(EditorConfigCompat49(editorConfig))
    super.beforeFirstNode(editorConfig)
  }

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    ruleCompat.beforeVisitChildNodes(node, autoCorrect, emit)
    super.beforeVisitChildNodes(node, autoCorrect, emit)
  }

  override fun afterVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    ruleCompat.afterVisitChildNodes(node, autoCorrect, emit)
    super.afterVisitChildNodes(node, autoCorrect, emit)
  }

  override fun afterLastNode() {
    ruleCompat.afterLastNode()
    super.afterLastNode()
  }
}
