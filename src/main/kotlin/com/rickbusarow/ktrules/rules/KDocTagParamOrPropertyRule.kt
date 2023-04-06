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
import com.rickbusarow.ktrules.rules.internal.psi.AT_PARAM
import com.rickbusarow.ktrules.rules.internal.psi.AT_PROPERTY
import com.rickbusarow.ktrules.rules.internal.psi.childrenDepthFirst
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.psi.KtTypeParameterListOwner
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Ensures that KDoc tags use `@property` for vals or vars, and `@param` for non-property
 * parameters.
 */
class KDocTagParamOrPropertyRule : Rule(id = "kdoc-tag-param-or-property") {

  private val tagNames by lazy(NONE) {
    setOf(KDocKnownTag.AT_PARAM, KDocKnownTag.AT_PROPERTY)
  }

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.KDOC) {

      val functionOrClass = node.treeParent.psi as? KtTypeParameterListOwner
        ?: return

      val paramNameToValOrVar = buildMap {

        functionOrClass.getValueParameters().forEach { param ->
          put(param.name!!, param.hasValOrVar())
        }
        functionOrClass.typeParameters.forEach { param ->
          put(param.name!!, false)
        }
      }

      node.childrenDepthFirst()
        .filter { it.isKDocTag() }
        .filter { it.findChildByType(ElementType.KDOC_TAG_NAME)?.text in tagNames }
        .toList()
        .forEach { tag ->

          visitKDocTag(tag, paramNameToValOrVar, emit, autoCorrect)
        }
    }
  }

  private fun visitKDocTag(
    tag: ASTNode,
    paramNameToValOrVar: Map<String, Boolean>,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    autoCorrect: Boolean
  ) {
    val tagTypeNameNode = tag.findChildByType(ElementType.KDOC_TAG_NAME) ?: return

    val tagTypeName = tagTypeNameNode.text

    val paramName by lazy(NONE) {
      tag.findChildByType(ElementType.KDOC_MARKDOWN_LINK)?.text
        ?: tag.text.substringAfter(tagTypeName).trim().substringBefore(" ")
    }

    val shouldBeProperty = paramNameToValOrVar[paramName] ?: false

    if (shouldBeProperty && tagTypeName != KDocKnownTag.AT_PROPERTY) {

      emit(
        tag.startOffset,
        "The KDoc tag '$tagTypeName $paramName' should use '${KDocKnownTag.AT_PROPERTY}'.",
        true
      )

      if (autoCorrect) {
        tag.fix(
          oldTagTypeNameNode = tagTypeNameNode,
          newTypeNameText = KDocKnownTag.AT_PROPERTY
        )
      }
    } else if (!shouldBeProperty && tagTypeName != KDocKnownTag.AT_PARAM) {

      emit(
        tag.startOffset,
        "The KDoc tag '$tagTypeName $paramName' should use '${KDocKnownTag.AT_PARAM}'.",
        true
      )
      if (autoCorrect) {
        tag.fix(
          oldTagTypeNameNode = tagTypeNameNode,
          newTypeNameText = KDocKnownTag.AT_PARAM
        )
      }
    }
  }

  private fun ASTNode.fix(
    oldTagTypeNameNode: ASTNode,
    newTypeNameText: String
  ) {
    replaceChild(
      oldTagTypeNameNode,
      LeafPsiElement(ElementType.KDOC_TAG_NAME, newTypeNameText).node
    )
  }
}
