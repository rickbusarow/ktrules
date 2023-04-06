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

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.Rule.VisitorModifier.RunAfterRule
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_START
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.nextLeaf
import com.pinterest.ktlint.core.ast.upsertWhitespaceBeforeMe
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Ensures that there's a space after every leading asterisk in a KDoc comment, except for blank
 * lines.
 */
class KDocIndentAfterLeadingAsteriskRule : Rule(
  id = "kdoc-indent-after-leading-asterisk",
  visitorModifiers = setOf(RunAfterRule("kdoc-leading-asterisk"))
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

      emit(node.startOffset, "kdoc leading asterisk", true)

      if (autoCorrect) {

        val existingSpaces = nextLeaf.text.takeWhile { it == ' ' }.length

        val fix = leading.drop(existingSpaces)

        nextLeaf.upsertWhitespaceBeforeMe(fix)
      }
    }
  }
}
