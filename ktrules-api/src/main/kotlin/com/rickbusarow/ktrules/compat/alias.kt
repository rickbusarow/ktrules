/*
 * Copyright (C) 2024 Rick Busarow
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

@file:Suppress("MaxLineLength")

package com.rickbusarow.ktrules.compat

/**
 * Creates a [RunAfterRule] that enforces this rule to run after the specified
 * rule regardless of whether the specified rule is loaded or disabled.
 *
 * @param ruleId The ID of the rule that this rule must run after.
 * @return A [RunAfterRule] with the specified rule ID and
 *   [com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED]
 *   mode.
 * @since 1.1.0
 */
public fun mustRunAfter(ruleId: RuleId): RuleCompat.VisitorModifierCompat.RunAfterRuleCompat =
  RuleCompat.VisitorModifierCompat.RunAfterRuleCompat(
    ruleId,
    RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
  )

/**
 * Creates a [RunAfterRule] that enforces this rule to run after the
 * specified rule only when the specified rule is both loaded and enabled.
 *
 * @param ruleId The ID of the rule that this rule depends on.
 * @return A [RunAfterRule] with the specified rule ID and
 *   [com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED]
 *   mode.
 * @since 1.1.0
 */
public fun dependsOn(ruleId: RuleId): RuleCompat.VisitorModifierCompat.RunAfterRuleCompat =
  RuleCompat.VisitorModifierCompat.RunAfterRuleCompat(
    ruleId,
    RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat.ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
  )
