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

package com.rickbusarow.ktrules.rules

import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC_START
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import com.pinterest.ktlint.rule.engine.core.api.nextLeaf
import com.rickbusarow.ktrules.KtRulesRuleSetProvider.Companion.ABOUT
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Ensures that there's a space after every leading asterisk in a KDoc comment, except for blank
 * lines.
 *
 * @since 1.0.4
 */
class KDocIndentAfterLeadingAsteriskRule : Rule(
  ID,
  ABOUT,
  visitorModifiers = setOf(
    RunAfterRule(
      KDocLeadingAsteriskRule.ID,
      REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
    )
  )
) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == KDOC_LEADING_ASTERISK || node.elementType == KDOC_START) {
      val nextLeaf = node.nextLeaf(true) ?: return

      val childOfTag = node.parents().any { it.isKDocTag() }

      // For anything after a tag, indent three spaces
      val leading = if (childOfTag) "   " else " "

      if (nextLeaf.isWhiteSpaceWithNewline() || nextLeaf.text.startsWith(leading)) {
        return
      }

      emit(node.startOffset, ERROR_MESSAGE, true)

      if (autoCorrect) {

        val trimmed = nextLeaf.text.trimStart()

        val newNode = LeafPsiElement(nextLeaf.elementType, "$leading$trimmed")

        nextLeaf.parent!!.replaceChild(nextLeaf, newNode)
      }
    }
  }

  internal companion object {

    val ID = RuleId("kt-rules:kdoc-indent-after-leading-asterisk")
    const val ERROR_MESSAGE = "kdoc indent after leading asterisk"
  }
}
