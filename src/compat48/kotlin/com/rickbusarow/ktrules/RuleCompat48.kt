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

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.EditorConfigProperties
import com.pinterest.ktlint.core.api.UsesEditorConfigProperties
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAsLateAsPossibleCompat
import com.rickbusarow.ktrules.rules.internal.mapToSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty as KtLintEditorConfigProperty

/** @since 1.1.1 */
class RuleCompat48(private val ruleCompat: RuleCompat) :
  Rule(
    id = ruleCompat.ruleId.value,
    visitorModifiers = ruleCompat.visitorModifiers.mapToSet { visitorModifier ->

      when (visitorModifier) {
        is RunAfterRuleCompat -> VisitorModifier.RunAfterRule(
          ruleId = visitorModifier.ruleId.value,
          loadOnlyWhenOtherRuleIsLoaded =
          visitorModifier.mode == ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED,
          runOnlyWhenOtherRuleIsEnabled =
          visitorModifier.mode != REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED

        )

        RunAsLateAsPossibleCompat -> VisitorModifier.RunAsLateAsPossible
      }
    }
  ),
  UsesEditorConfigProperties {

  override val editorConfigProperties: List<KtLintEditorConfigProperty<*>>
    get() = ruleCompat.usesEditorConfigProperties.map { it.toKtLintProperty48() }

  override fun beforeFirstNode(editorConfigProperties: EditorConfigProperties) {
    ruleCompat.beforeFirstNode(EditorConfigCompat48(editorConfigProperties))
    super.beforeFirstNode(editorConfigProperties)
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
