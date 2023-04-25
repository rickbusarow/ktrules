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

import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.psi.childrenDepthFirst
import com.rickbusarow.ktrules.rules.internal.psi.fileIndent
import com.rickbusarow.ktrules.rules.internal.psi.isKDocEnd
import com.rickbusarow.ktrules.rules.internal.psi.isKDocLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.isKDocWhitespaceBeforeLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpace
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpaceWithNewline
import com.rickbusarow.ktrules.rules.internal.psi.nextLeaf
import com.rickbusarow.ktrules.rules.internal.psi.nextSibling
import com.rickbusarow.ktrules.rules.internal.psi.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.psi.api.KDoc

/**
 * Ensures that each line of a KDoc comment begins with `* ` (after the indent spaces).
 *
 * @since 1.0.1
 */
class KDocLeadingAsteriskRule : RuleCompat(ID,) {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.KDOC_START) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi.parent as KDoc

    val indent = kdoc.fileIndent(additionalOffset = 1)
    val newlineIndent = "\n$indent"

    kdoc.node.childrenDepthFirst()
      .distinct()
      .filter { it.elementType == ElementType.WHITE_SPACE }
      .filter { it.text.lines().size > 1 }
      .filter { !it.isKDocWhitespaceBeforeLeadingAsterisk() }
      .filter { node ->
        val next = node.nextLeaf()

        when {
          next == null -> false
          next.isWhiteSpace() -> false
          next.isKDocLeadingAsterisk() -> false
          next.isKDocEnd() && node.text.lines().count() <= 2 -> false
          else -> true
        }
      }
      .toList()
      .forEach { node ->

        val parent = node.parent!!

        val next = node.nextSibling() ?: return@forEach

        emit(next.startOffset, ERROR_MESSAGE, true)

        if (autoCorrect) {

          val numLines = node.text.split("\n").size - 1

          parent.removeChild(node)

          repeat(numLines) { i ->
            if (i == numLines - 1 && next.elementType == ElementType.KDOC_END) {
              parent.addChild(PsiWhiteSpaceImpl(newlineIndent.dropLast(1)), next)
            } else {
              parent.addChild(PsiWhiteSpaceImpl(newlineIndent), next)
              parent.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), next)
            }
          }

          if (!next.isWhiteSpaceWithNewline()) {
            parent.addChild(PsiWhiteSpaceImpl(" "), next)
          }
        }
      }
  }

  internal companion object {
    val ID = RuleId("kdoc-leading-asterisk")
    const val ERROR_MESSAGE = "kdoc leading asterisk"
  }
}
