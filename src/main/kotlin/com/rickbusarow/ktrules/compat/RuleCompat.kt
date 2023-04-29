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

import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * A stable compatibility shim for KtLint's `Rule` classes. It's implemented by `Rule47`, `Rule48`,
 * and `Rule49` in the different split source sets.
 *
 * @since 1.1.1
 */
@Suppress("UnnecessaryAbstractClass")
abstract class RuleCompat(
  /**
   * Identification of the rule. A [ruleId] has a value that must adhere the convention
   * "<rule-set-id>:<rule-id>". The rule set id 'standard' is reserved for rules which are
   * maintained by the KtLint project. Rules created by custom rule set providers and API Consumers
   * should use a prefix other than 'standard' to mark the origin of rules which are not maintained
   * by the KtLint project.
   *
   * @since 1.1.1
   */
  val ruleId: RuleId,

  /**
   * Set of modifiers of the visitor. Preferably a rule has no modifiers at all, meaning that it is
   * completely independent of all other rules.
   *
   * @since 1.1.1
   */
  val visitorModifiers: Set<RuleCompat.VisitorModifierCompat> = setOf(),

  /**
   * Set of [EditorConfigProperty]'s that are to provided to the rule. Only specify the properties
   * that are actually used by the rule.
   *
   * @since 1.1.1
   */
  val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = setOf()
) {
  /**
   * This method is called once before the first node is visited. It can be used to initialize the
   * state of the rule before processing of nodes starts.
   *
   * @since 1.1.1
   */
  open fun beforeFirstNode(editorConfig: EditorConfigCompat) {}

  /**
   * This method is called on a node in AST before visiting the child nodes. This is repeated
   * recursively for the child nodes resulting in a depth first traversal of the AST.
   *
   * @param node AST node
   * @param autoCorrect indicates whether rule should attempt autocorrection
   * @param emit a way for rule to notify about a violation (lint error)
   * @since 1.1.1
   */
  open fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
  ) {
  }

  /**
   * This method is called on a node in AST after all its child nodes have been visited.
   *
   * @since 1.1.1
   */
  open fun afterVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
  ) {
  }

  /**
   * This method is called once after the last node in the AST is visited. It can be used for
   * teardown of the state of the rule.
   *
   * @since 1.1.1
   */
  open fun afterLastNode() {}

  /** @since 1.1.1 */
  sealed class VisitorModifierCompat {
    /**
     * Defines that the [Rule] that declares this [VisitorModifierCompat] will be run after the
     * [Rule] with rule id [VisitorModifierCompat.RunAfterRuleCompat.ruleId].
     *
     * @since 1.1.1
     */
    data class RunAfterRuleCompat(
      /**
       * The [RuleId] of the [Rule] which should run before the [Rule] that declares the
       * [VisitorModifierCompat.RunAfterRuleCompat].
       *
       * @since 1.1.1
       */
      val ruleId: RuleId,
      /**
       * The [ModeCompat] determines whether the [Rule] that declares this [VisitorModifierCompat]
       * can be run in case the [Rule] with rule id
       * [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is not loaded or enabled.
       *
       * @since 1.1.1
       */
      val mode: ModeCompat,
    ) : VisitorModifierCompat() {
      /** @since 1.1.1 */
      enum class ModeCompat {
        /**
         * Run the [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat] regardless
         * whether the [Rule] with ruleId [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is
         * loaded or disabled. However, if that other rule is loaded and enabled, it runs before the
         * [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat].
         *
         * @since 1.1.1
         */
        REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,

        /**
         * Run the [Rule] that declares the [VisitorModifierCompat.RunAfterRuleCompat] only in case
         * the [Rule] with ruleId [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is loaded *and*
         * enabled. That other rule runs before the [Rule] that declares the
         * [VisitorModifierCompat.RunAfterRuleCompat].
         *
         * @since 1.1.1
         */
        ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED,
      }
    }

    /** @since 1.1.1 */
    object RunAsLateAsPossibleCompat : VisitorModifierCompat()
  }
}

/** @since 1.1.1 */
@Suppress("UndocumentedPublicProperty")
@JvmInline
value class RuleId(val value: String) : java.io.Serializable
