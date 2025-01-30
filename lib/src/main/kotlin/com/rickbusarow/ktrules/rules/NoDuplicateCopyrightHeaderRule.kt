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

package com.rickbusarow.ktrules.rules

import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.psi.childrenBreadthFirst
import com.rickbusarow.ktrules.rules.internal.psi.isCopyrightHeader
import com.rickbusarow.ktrules.rules.internal.psi.isFile
import com.rickbusarow.ktrules.rules.internal.psi.isScript
import com.rickbusarow.ktrules.rules.internal.psi.isTopLevel
import com.rickbusarow.ktrules.rules.internal.psi.nextLeaf
import com.rickbusarow.ktrules.rules.internal.psi.parentsWithSelf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Ensures that a file hasn't gotten into a wonky state where there
 * are two consecutive copyright headers, or a copyright header, then
 * a package declaration or imports, then another copyright header.
 *
 * This can happen somewhat reliably with build.gradle.kts files which have imports.
 *
 * @since 1.0.1
 */
class NoDuplicateCopyrightHeaderRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.FILE) {

      node.childrenBreadthFirst { child ->

        // In a script file, the second copyright block will be attached to whatever comes after it.
        // So the comment's parent will be a block, property, etc.,
        // and that element's parent will be a SCRIPT.  The script's parent is the file.
        @Suppress("MagicNumber")
        child.isFile() ||
          child.isTopLevel() ||
          child.parentsWithSelf()
            .take(3)
            .any { it.isScript() }
      }
        .filter { it.isCopyrightHeader() }
        // Sort by their position so that the actual first one is kept.
        .sortedBy { it.startOffset }
        .drop(1)
        .forEach { commentNode ->

          val trailingWhitespace = commentNode.nextLeaf(includeEmpty = true)

          emit(commentNode.startOffset, ERROR_MESSAGE, true)
          node.removeChild(commentNode)
          if (trailingWhitespace != null) {
            node.removeChild(trailingWhitespace)
          }
        }
    }
  }

  internal companion object {
    val ID = RuleId("no-duplicate-copyright-header")
    const val ERROR_MESSAGE = "duplicate copyright header"
  }
}
