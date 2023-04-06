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
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.rickbusarow.ktrules.rules.internal.psi.childrenDepthFirst
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.sortedWith
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters

/** Sorts KDoc tags by their declaration order in the class or function. */
class KDocTagOrderRule : Rule(id = "kdoc-tag-order") {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.KDOC) {

      val functionOrClass = node.treeParent.psi as? KtTypeParameterListOwner
        ?: return

      val parameterNames = functionOrClass.typeParameters.map { it.name!! }
        .plus(functionOrClass.getValueParameters().map { it.name!! })

      val kdocTags = node.childrenDepthFirst().filter { it.isKDocTag() }.toList()

      val sortedKdocTags = kdocTags.sortedWith(
        { tag ->
          val tagName = tag.findChildByType(KDOC_TAG_NAME)?.text

          tagName !in setOf("@param", "@property")
        },
        { tag ->

          when (val tagName = tag.findChildByType(KDOC_TAG_NAME)?.text.orEmpty()) {
            "@param", "@property" -> {
              val paramName = tag.text.substringAfter(tagName).trim().substringBefore(" ")
              parameterNames.indexOf(paramName).toString()
            }

            else -> tagName
          }
        }
      )

      kdocTags.forEachIndexed { index, kdocTag ->

        if (kdocTag != sortedKdocTags[index]) {
          val tagName = kdocTag.findChildByType(KDOC_TAG_NAME)?.text.orEmpty()
          emit(
            kdocTag.startOffset,
            "KDoc tag order is incorrect. $tagName should be sorted.",
            true
          )

          if (autoCorrect) {
            val sortedTag = sortedKdocTags[index].clone() as ASTNode
            kdocTag.treeParent.replaceChild(kdocTag, sortedTag)

            // Ensure correct whitespace before the sorted tag
            val prevTag = sortedTag.treePrev ?: return@forEachIndexed
            if (prevTag.elementType == WHITE_SPACE && prevTag.text != "\n") {
              val newWhiteSpace = LeafPsiElement(WHITE_SPACE, "\n")
              sortedTag.treeParent.replaceChild(prevTag, newWhiteSpace)
            }
          }
        }
      }
    }
  }
}
