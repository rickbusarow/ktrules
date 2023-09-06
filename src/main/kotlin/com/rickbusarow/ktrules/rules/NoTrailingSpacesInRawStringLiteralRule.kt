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
import com.rickbusarow.ktrules.rules.internal.psi.nextLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Ensures that there are no trailing whitespaces in raw string templates.
 *
 * ```
 * // Bad code (using '·' as the whitespace)
 * """·
 * Hello·world,··
 * ·
 * How's·it·going?
 * """
 *
 * // good code
 * """
 * Hello·world,
 *
 * How's·it·going?
 * """
 * ```
 *
 * @since 1.0.1
 */
class NoTrailingSpacesInRawStringLiteralRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.LITERAL_STRING_TEMPLATE_ENTRY) {

      val stringPartNode = node.nextLeaf(true) ?: return

      if (!stringPartNode.hasTrailingSpaces()) return

      if (stringPartNode.nextLeaf(true)?.text != "\n") return

      val violationOffset = stringPartNode.startOffset + stringPartNode.text.trimEnd().length
      emit(violationOffset, ERROR_MESSAGE, true)

      stringPartNode.removeTrailingSpaces()
    }
  }

  private fun ASTNode.hasTrailingSpaces() = text.hasTrailingSpace()

  private fun ASTNode.removeTrailingSpaces() {
    val newText = text.trimEnd()
    (this as LeafPsiElement).rawReplaceWithText(newText)
  }

  private fun String.hasTrailingSpace() = takeLast(1) == " "

  internal companion object {
    val ID = RuleId("no-trailing-space-in-raw-string-literal")
    const val ERROR_MESSAGE = "Trailing space(s) in literal string template"
  }
}
