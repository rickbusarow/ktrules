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
import com.rickbusarow.ktrules.compat.mustRunAfter
import com.rickbusarow.ktrules.rules.internal.psi.childrenDepthFirst
import com.rickbusarow.ktrules.rules.internal.psi.getValueParameters
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpace
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpaceWithNewline
import com.rickbusarow.ktrules.rules.internal.sortedWith
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner

/**
 * Sorts KDoc tags by their declaration order in the class or function.
 *
 * @since 1.0.4
 */
class KDocTagOrderRule : RuleCompat(
  ruleId = ID,
  visitorModifiers = setOf(
    mustRunAfter(NoSinceInKDocRule.ID),
    mustRunAfter(KDocTagParamOrPropertyRule.ID)
  )
) {

  override fun beforeVisitChildNodes(node: ASTNode, emit: EmitWithDecision) {

    if (node.elementType == ElementType.KDOC) {

      val functionOrClass = node.treeParent.psi as? KtTypeParameterListOwner
        ?: return

      val parameterNames = functionOrClass.typeParameters.map { it.name!! }
        .plus(functionOrClass.getValueParameters().map { it.name!! })

      val kdocTags = node.childrenDepthFirst().filter { it.isKDocTag() }.toList()

      val sortedKdocTags = kdocTags.sortedWith(
        { tag ->
          val tagName = tag.findChildByType(ElementType.KDOC_TAG_NAME)?.text

          tagName !in setOf("@param", "@property")
        },
        { tag ->

          when (val tagName = tag.findChildByType(ElementType.KDOC_TAG_NAME)?.text.orEmpty()) {
            "@param", "@property" -> {

              val paramName = tag.findChildByType(ElementType.KDOC_MARKDOWN_LINK)?.text
                ?: tag.text.substringAfter(tagName).trim().substringBefore(" ")
              // Treat parameter indices as numbers since they'll be sorted ahead of strings,
              // but pad them with zeros ('01', '02', etc.) so that '10' isn't sorted ahead of '2'.
              @Suppress("MagicNumber")
              parameterNames.indexOf(paramName).toString().padStart(3, '0')
            }

            else -> tagName
          }
        }
      )

      kdocTags.forEachIndexed { index, kdocTag ->

        if (kdocTag != sortedKdocTags[index]) {
          val tagName = kdocTag.findChildByType(ElementType.KDOC_TAG_NAME)?.text.orEmpty()
          emit(
            kdocTag.startOffset,
            "KDoc tag order is incorrect. $tagName should be sorted.",
            true
          )
            .ifAutocorrectAllowed {
              val sortedTag = sortedKdocTags[index].clone() as ASTNode
              kdocTag.treeParent.replaceChild(kdocTag, sortedTag)

              // Ensure correct whitespace before the sorted tag
              val prevTag = sortedTag.treePrev ?: return@forEachIndexed
              if (prevTag.isWhiteSpace() && !prevTag.isWhiteSpaceWithNewline()) {
                val newWhiteSpace = LeafPsiElement(ElementType.WHITE_SPACE, "\n")
                sortedTag.treeParent.replaceChild(prevTag, newWhiteSpace)
              }
            }
        }
      }
    }
  }

  internal companion object {
    val ID = RuleId("kdoc-tag-order")
  }
}
