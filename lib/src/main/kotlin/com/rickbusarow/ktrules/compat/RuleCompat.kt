/*
 * Copyright (C) 2025 Rick Busarow
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

import com.rickbusarow.ktrules.compat.RuleCompat.AutocorrectDecisionCompat
import com.rickbusarow.ktrules.compat.RuleCompat.EmitWithDecision
import com.rickbusarow.ktrules.compat.RuleCompat.VisitorModifierCompat.RunAfterRuleCompat.ModeCompat
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * A stationary version of KtLint's `RuleProvider`, except it returns [RuleCompat]
 *
 * @since 1.1.1
 */
fun interface RuleProviderCompat {
  /**
   * Creates a new [RuleCompat]
   *
   * @since 1.1.1
   */
  fun createNewRuleInstance(): RuleCompat
}

/** Compat for the deprecated 'emit' lambdas that return Unit. Use EmitWithDecision instead.*/
@Deprecated(
  "Compat for the deprecated 'emit' lambdas that return Unit.  Use EmitWithDecision instead.",
  ReplaceWith("EmitWithDecision")
)
typealias EmitWithUnit = (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit

/**
 * A stable compatibility shim for KtLint's `Rule` classes. It's implemented
 * by `Rule47`, `Rule48`, and `Rule49` in the different split source sets.
 *
 * @since 1.1.1
 */
@Suppress("UnnecessaryAbstractClass")
abstract class RuleCompat(
  /**
   * Identification of the rule. A [ruleId] has a value that must adhere the
   * convention "<rule-set-id>:<rule-id>". The rule set id 'standard' is reserved
   * for rules which are maintained by the KtLint project. Rules created by custom
   * rule set providers and API Consumers should use a prefix other than 'standard'
   * to mark the origin of rules which are not maintained by the KtLint project.
   *
   * @since 1.1.1
   */
  val ruleId: RuleId,

  /**
   * Set of modifiers of the visitor. Preferably a rule has no modifiers
   * at all, meaning that it is completely independent of all other rules.
   *
   * @since 1.1.1
   */
  val visitorModifiers: Set<VisitorModifierCompat> = emptySet(),

  /**
   * Set of [EditorConfigProperty]'s that are to provided to the rule.
   * Only specify the properties that are actually used by the rule.
   *
   * @since 1.1.1
   */
  val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet()
) {
  /**
   * This method is called once before the first node is visited. It can be
   * used to initialize the state of the rule before processing of nodes starts.
   *
   * @since 1.1.1
   */
  open fun beforeFirstNode(editorConfig: EditorConfigCompat) = Unit

  /**
   * This method is called on a node in AST before visiting the child nodes. This is repeated
   * recursively for the child nodes resulting in a depth first traversal of the AST.
   *
   * @param node AST node
   * @param autoCorrect indicates whether rule should attempt autocorrection
   * @param emit a way for rule to notify about a violation (lint error)
   * @since 1.1.1
   */
  @Suppress("DEPRECATION")
  @Deprecated(
    "Marked for removal in KtLint 2.0.0.  Use the overload without `autoCorrect` parameter instead.",
    ReplaceWith("beforeVisitChildNodes(node, emit)")
  )
  open fun beforeVisitChildNodes(node: ASTNode, autoCorrect: Boolean, emit: EmitWithUnit) = Unit

  /**
   * This method is called on a node in AST before visiting the child nodes. This is repeated
   * recursively for the child nodes resulting in a depth first traversal of the AST.
   *
   * When a rule overrides this method, the API Consumer can decide per violation
   * whether the violation needs to be autocorrected. For this the [emit] function
   * is called, and its result can be used to determine whether the violation
   * is to be corrected. In lint mode the [emit] should always return false.
   *
   * @param node AST node
   * @param emit a way for rule to notify about a violation (lint error) and get
   *   approval to actually autocorrect the violation if that is supported by the rule
   */
  open fun beforeVisitChildNodes(node: ASTNode, emit: EmitWithDecision) = Unit

  /**
   * This method is called on a node in AST after all its child nodes have been visited.
   *
   * @since 1.1.1
   */
  @Suppress("DEPRECATION")
  @Deprecated(
    "Marked for removal in KtLint 2.0.0.  Use the overload without `autoCorrect` parameter instead.",
    ReplaceWith("afterVisitChildNodes(node, emit)")
  )
  open fun afterVisitChildNodes(node: ASTNode, autoCorrect: Boolean, emit: EmitWithUnit) = Unit

  /**
   * This method is called on a node in AST after all its child nodes have been visited.
   *
   * When a rule overrides this method, the API Consumer can decide per violation
   * whether the violation needs to be autocorrected. For this the [emit] function
   * is called, and its result can be used to determine whether the violation
   * is to be corrected. In lint mode the [emit] should always return false.
   *
   * @param node AST node
   * @param emit a way for rule to notify about a violation (lint error) and get
   *   approval to actually autocorrect the violation if that is supported by the rule
   */
  open fun afterVisitChildNodes(node: ASTNode, emit: EmitWithDecision) = Unit

  /**
   * This method is called once after the last node in the AST is
   * visited. It can be used for teardown of the state of the rule.
   *
   * @since 1.1.1
   */
  open fun afterLastNode() = Unit

  /** @since 1.1.1 */
  sealed class VisitorModifierCompat {
    /**
     * Defines that the [RuleCompat] that declares this [VisitorModifierCompat] will be run
     * after the [RuleCompat] with rule id [VisitorModifierCompat.RunAfterRuleCompat.ruleId].
     *
     * @since 1.1.1
     */
    data class RunAfterRuleCompat(
      /**
       * The [RuleId] of the [RuleCompat] which should run before the [RuleCompat]
       * that declares the [VisitorModifierCompat.RunAfterRuleCompat].
       *
       * @since 1.1.1
       */
      val ruleId: RuleId,
      /**
       * The [ModeCompat] determines whether the [RuleCompat] that declares this
       * [VisitorModifierCompat] can be run in case the [RuleCompat] with rule id
       * [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is not loaded or enabled.
       *
       * @since 1.1.1
       */
      val mode: ModeCompat
    ) : VisitorModifierCompat() {
      /** @since 1.1.1 */
      enum class ModeCompat {
        /**
         * Run the [RuleCompat] that declares the [VisitorModifierCompat.RunAfterRuleCompat]
         * regardless whether the [RuleCompat] with ruleId
         * [VisitorModifierCompat.RunAfterRuleCompat.ruleId] is loaded or disabled.
         * However, if that other rule is loaded and enabled, it runs before the
         * [RuleCompat] that declares the [VisitorModifierCompat.RunAfterRuleCompat].
         *
         * @since 1.1.1
         */
        REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,

        /**
         * Run the [RuleCompat] that declares the [VisitorModifierCompat.RunAfterRuleCompat] only
         * in case the [RuleCompat] with ruleId [VisitorModifierCompat.RunAfterRuleCompat.ruleId]
         * is loaded *and* enabled. That other rule runs before the [RuleCompat]
         * that declares the [VisitorModifierCompat.RunAfterRuleCompat].
         *
         * @since 1.1.1
         */
        ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED
      }
    }

    /** @since 1.1.1 */
    data object RunAsLateAsPossibleCompat : VisitorModifierCompat()
  }

  /**
   * A strong type for the `emit` lambdas that return an [AutocorrectDecisionCompat].
   *
   * @see RuleCompat.beforeVisitChildNodes
   * @see RuleCompat.afterVisitChildNodes
   */
  fun interface EmitWithDecision : (Int, String, Boolean) -> AutocorrectDecisionCompat {
    override operator fun invoke(
      offset: Int,
      errorMessage: String,
      canBeAutoCorrected: Boolean
    ): AutocorrectDecisionCompat
  }

  /** */
  enum class AutocorrectDecisionCompat {

    /** */
    ALLOW_AUTOCORRECT,

    /** */
    NO_AUTOCORRECT;

    /** */
    inline fun <T> ifAutocorrectAllowed(function: () -> T): T? =
      takeIf { this == ALLOW_AUTOCORRECT }
        ?.let { function() }
  }
}

/** */
fun EmitWithUnit.toEmitWithDecision(autoCorrect: Boolean): EmitWithDecision {
  return EmitWithDecision { offset, errorMessage, canBeAutoCorrected ->
    this(offset, errorMessage, canBeAutoCorrected)
    if (canBeAutoCorrected && autoCorrect) {
      AutocorrectDecisionCompat.ALLOW_AUTOCORRECT
    } else {
      AutocorrectDecisionCompat.NO_AUTOCORRECT
    }
  }
}

/** @since 1.1.1 */
@Suppress("UndocumentedPublicProperty")
@JvmInline
value class RuleId(val value: String) : java.io.Serializable
