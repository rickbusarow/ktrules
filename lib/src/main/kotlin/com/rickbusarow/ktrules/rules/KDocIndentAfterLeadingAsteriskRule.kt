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

import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.compat.mustRunAfter
import com.rickbusarow.ktrules.rules.internal.psi.isKDocLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.isKDocStart
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpaceWithNewline
import com.rickbusarow.ktrules.rules.internal.psi.nextLeaf
import com.rickbusarow.ktrules.rules.internal.psi.parent
import com.rickbusarow.ktrules.rules.internal.psi.parents
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Ensures that there's a space after every leading
 * asterisk in a KDoc comment, except for blank lines.
 *
 * @since 1.0.4
 */
class KDocIndentAfterLeadingAsteriskRule : RuleCompat(
  ID,
  visitorModifiers = setOf(
    mustRunAfter(KDocLeadingAsteriskRule.ID)
  )
) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.isKDocLeadingAsterisk() || node.isKDocStart()) {
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

    val ID = RuleId("kdoc-indent-after-leading-asterisk")
    const val ERROR_MESSAGE = "kdoc indent after leading asterisk"
  }
}
