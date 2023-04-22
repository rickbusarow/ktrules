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

import com.rickbusarow.ktrules.KtRulesRuleSetProvider.Companion.ABOUT
import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.Rule
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.psi.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Ensures that there are no blank lines at the *start* of a kotlin file.
 *
 * @since 1.0.1
 */
class NoLeadingBlankLinesRule : Rule(ID, ABOUT) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.FILE) {

      node.children()
        .takeWhile { it.text.isBlank() }
        // KtFiles always have a package directive and imports list, even if they're empty/blank.
        // In the case of a file without either, the list of ASTNode element types would be:
        // [PACKAGE_DIRECTIVE, IMPORT_LIST, WHITE_SPACE, CLASS, WHITE_SPACE]
        // So, leave those blank package and import nodes alone.  Delete the first white space and the
        // file will look the way it's supposed to.
        .filter { it.elementType == ElementType.WHITE_SPACE }
        .forEach { leadingWhiteSpace ->
          emit(leadingWhiteSpace.startOffset, ERROR_MESSAGE, true)
          node.removeChild(leadingWhiteSpace)
        }
    }
  }

  internal companion object {
    val ID = RuleId("kt-rules:no-leading-blank-lines")
    const val ERROR_MESSAGE = "leading blank lines"
  }
}
