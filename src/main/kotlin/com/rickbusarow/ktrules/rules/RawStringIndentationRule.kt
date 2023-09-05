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

import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.isPrinted
import com.rickbusarow.ktrules.rules.internal.psi.ASTTreePrinter.Companion.printEverything
import com.rickbusarow.ktrules.rules.internal.psi.children
import com.rickbusarow.ktrules.rules.internal.psi.fileIndent
import com.rickbusarow.ktrules.rules.internal.psi.isRawString
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

class RawStringIndentationRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    if (!node.isRawString()) return

    node.printEverything('·')

    val text = node.text

    val fileIndent = node.fileIndent(0)
    val indentSize = fileIndent.length

    val lines = text.lines()

    for (line in node.children()) {

      val lineText = line.text

      val internalLineIndent = lineText.indexOfFirst { it.isPrinted() && !it.isWhitespace() }

      if (lineText.isNotBlank() && internalLineIndent < indentSize) {
        emit(node.startOffset, "Incorrect indentation for multiline raw string", true)
        if (autoCorrect) {
          (node as LeafPsiElement).rawReplaceWithText(
            lines.mapIndexed { index, s ->
              if (index != 0 && s.isNotBlank()) fileIndent + s else s
            }.joinToString("\n")
          )
        }
      }
    }
  }

  internal companion object {
    val ID = RuleId("raw-string-indentation")
    internal const val TRIPLE_QUOTES = "\"\"\""
  }
}
