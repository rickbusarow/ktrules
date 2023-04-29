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
class KDocLeadingAsteriskRule : RuleCompat(ID) {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.KDOC) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi as KDoc

    val indent = kdoc.fileIndent(additionalOffset = 1)
    val newlineIndent = "\n$indent"

    kdocNode.childrenDepthFirst()
      .filter { it.elementType == ElementType.WHITE_SPACE }
      .filter { it.text.lines().size > 1 }
      .filter { !(it.isKDocWhitespaceBeforeLeadingAsterisk() && it.text.lines().size == 2) }
      .filter { node ->
        val next = node.nextLeaf()
        val nodeLines = node.text.lines()

        when {
          next == null -> false
          next.isWhiteSpace() -> false
          next.isKDocLeadingAsterisk() && nodeLines.size <= 2 -> false
          next.isKDocEnd() && nodeLines.size <= 2 -> false
          else -> true
        }
      }
      .toList()
      .forEach { node ->

        val next = node.nextLeaf(true) ?: return@forEach

        emit(next.startOffset, ERROR_MESSAGE, true)

        if (autoCorrect) {

          node.fixBlankLines(next, newlineIndent)
        }
      }
  }

  private fun ASTNode.fixBlankLines(next: ASTNode, newlineIndent: String) {
    val numLines = text.count { it == '\n' }

    val nodeParent = parent!!
    val nextParent = next.parent!!

    nodeParent.removeChild(this)

    (1..numLines).forEach { i ->
      when {
        i == numLines && next.isKDocEnd() -> {
          nextParent.addChild(PsiWhiteSpaceImpl(newlineIndent), next)
        }

        i == numLines && next.isKDocLeadingAsterisk() -> {
          nextParent.addChild(PsiWhiteSpaceImpl(newlineIndent.dropLast(1)), next)
        }

        else -> {
          nextParent.addChild(PsiWhiteSpaceImpl(newlineIndent), next)
          nextParent.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), next)
        }
      }
    }

    if (!next.isWhiteSpaceWithNewline() && !next.isKDocEnd()) {
      nextParent.addChild(PsiWhiteSpaceImpl(" "), next)
    }
  }

  internal companion object {
    val ID = RuleId("kdoc-leading-asterisk")
    const val ERROR_MESSAGE = "kdoc leading asterisk"
  }
}
