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

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.EditorConfigProperties
import com.pinterest.ktlint.core.api.UsesEditorConfigProperties
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.rickbusarow.ktrules.compat.VisitorModifierCompat.RunAsLateAsPossibleCompat

actual abstract class RuleCompat actual constructor(
  ruleId: RuleId,
  visitorModifiers: Set<VisitorModifierCompat>,
  usesEditorConfigProperties: Set<EditorConfigProperty<*>>
) : Rule(id = ruleId.value, visitorModifiers = visitorModifiers.mapToSet { visitorModifier ->

  when (visitorModifier) {
    is RunAfterRuleCompat -> VisitorModifier.RunAfterRule(
      ruleId = visitorModifier.ruleId.value,
      loadOnlyWhenOtherRuleIsLoaded = visitorModifier.mode == ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED,
      runOnlyWhenOtherRuleIsEnabled = visitorModifier.mode != REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED

    )

    RunAsLateAsPossibleCompat -> VisitorModifier.RunAsLateAsPossible
  }
}),
  UsesEditorConfigProperties {

  override val editorConfigProperties: List<UsesEditorConfigProperties.EditorConfigProperty<*>>
    by lazy { usesEditorConfigProperties.map { it.toKtLintProperty() } }

  /** */
  actual open fun beforeFirstNode(editorConfigProperties: EditorConfigCompat) = Unit

  override fun beforeFirstNode(editorConfigProperties: EditorConfigProperties) {
    beforeFirstNode(EditorConfigCompat47(editorConfigProperties))
    super.beforeFirstNode(editorConfigProperties)
  }
}
