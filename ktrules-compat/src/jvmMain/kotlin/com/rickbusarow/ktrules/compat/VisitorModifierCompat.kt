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

/** */
sealed class VisitorModifierCompat {
  /**
   * Defines that the [Rule] that declares this [VisitorModifierCompat] will be run after the
   * [Rule] with rule id [VisitorModifierCompat.RunAfterRuleCompat.ruleId].
   */
  data class RunAfterRuleCompat(
    /**
     * The [RuleId] of the [Rule] which should run before the [Rule] that declares the
     * [VisitorModifierCompat.RunAfterRuleCompat].
     */
    val ruleId: RuleId,
    /**
     * The [ModeCompat] determines whether the [Rule] that declares this [VisitorModifierCompat]
     * can be run in case the [Rule] with rule id
     * [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is not loaded or enabled.
     */
    val mode: ModeCompat,
  ) : VisitorModifierCompat() {
    /** */
    enum class ModeCompat {
      /**
       * Run the [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat] regardless
       * whether the [Rule] with ruleId [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is
       * loaded or disabled. However, if that other rule is loaded and enabled, it runs before the
       * [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat].
       */
      REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,

      /**
       * Run the [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat] only in case
       * the [Rule] with ruleId [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is loaded *and*
       * enabled. That other rule runs before the [Rule] that declares the
       * [VisitorModifierCompat.RunAfterRuleCompat].
       */
      ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED,
    }
  }

  /** */
  object RunAsLateAsPossibleCompat : VisitorModifierCompat()
}
