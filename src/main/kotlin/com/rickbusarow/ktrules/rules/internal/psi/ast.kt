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

package com.rickbusarow.ktrules.rules.internal.psi

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.KDOC_CODE_BLOCK_TEXT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_MARKDOWN_LINK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_START
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevSibling
import com.rickbusarow.ktrules.rules.internal.trees.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/** */
internal fun ASTNode?.isBlank(): Boolean = this != null && text.isBlank()

/** */
private val copyRightCommentStart = Regex(
  """(?:\/\*{1,2}\s+(?:\*\s)?|\/\/ *)Copyright [\s\S]*"""
)

/** */
internal fun ASTNode.isCopyrightHeader(): Boolean {
  if (elementType != ElementType.BLOCK_COMMENT) return false

  return text.matches(copyRightCommentStart)
}

/** */
internal val ASTNode.parent: ASTNode? get() = treeParent

/** */
internal fun ASTNode.isFirstChild(): Boolean = prevSibling() == null

/** */
internal fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }

/** */
internal fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }

/** */
internal fun ASTNode?.isKDocText(): Boolean = this != null && elementType == KDOC_TEXT

/** */
internal fun ASTNode?.isKDocTag(): Boolean = this != null && elementType == KDOC_TAG

/** */
internal fun ASTNode?.isKDocSectionWithTagChildren(): Boolean =
  this != null && elementType == KDOC_SECTION && children().any { it.isKDocTag() }

/** */
internal fun ASTNode?.isKDocTagWithTagChildren(): Boolean =
  this != null && elementType == KDOC_TAG && children().any { it.isKDocTag() }

/** */
internal fun ASTNode?.isKDocSection(): Boolean = this != null && elementType == KDOC_SECTION

/** */
internal fun ASTNode?.isKDocTagOrSection(): Boolean = isKDocSection() || isKDocTag()

/** */
internal fun ASTNode?.isKDocTagName(): Boolean = this != null && elementType == KDOC_TAG_NAME

/** */
internal fun ASTNode?.isKDocTagLink(): Boolean {
  return this != null && elementType == IDENTIFIER && parent?.elementType == KDOC_NAME
}

/** */
internal fun ASTNode?.isKDocMarkdownLink(): Boolean =
  this != null && elementType == KDOC_MARKDOWN_LINK

/** */
internal fun ASTNode?.isKDocDefaultSection(): Boolean = this?.psi?.isKDocDefaultSection() == true

/** */
internal fun ASTNode?.isKDocFirstSectionAfterDefault(): Boolean {
  return when {
    this == null -> false
    isKDocTag() -> parent?.prevSibling().isKDocDefaultSection()
    isKDocSection() -> prevSibling().isKDocDefaultSection()
    else -> false
  }
}

/** */
internal fun ASTNode?.isKDocLastSection(): Boolean {

  return when {
    this == null -> false
    isKDocTag() -> parent?.nextSibling().isKDocDefaultSection()
    isKDocSection() -> nextSibling().isKDocDefaultSection()
    else -> false
  }
}

/** */
internal fun ASTNode?.getKDocSection(): ASTNode? {
  return this?.parents()?.firstOrNull { it.isKDocSection() }
}

/** */
internal fun ASTNode?.isInKDocTag(): Boolean {
  return this != null && parents().any { it.isKDocTag() }
}

/** */
internal fun ASTNode?.isInKDocDefaultSection(): Boolean {
  if (this == null) return false

  val kdoc = psi.getNonStrictParentOfType<KDoc>() ?: return false

  val defaultSection = kdoc.getDefaultSection().node

  return this == defaultSection || parents().any { it == defaultSection }
}

/** */
internal fun ASTNode?.isKDocWhitespaceAfterLeadingAsterisk(): Boolean =
  this != null && (isWhiteSpace() || isBlank()) && prevSibling().isKDocLeadingAsterisk()

/** */
internal fun ASTNode?.isKDocWhitespaceBeforeLeadingAsterisk(): Boolean =
  this != null && elementType == WHITE_SPACE && nextSibling().isKDocLeadingAsterisk()

/** */
internal fun ASTNode?.isKDocLeadingAsterisk(): Boolean =
  this != null && elementType == KDOC_LEADING_ASTERISK

/** */
internal fun ASTNode?.isKDocEnd(): Boolean = this != null && elementType == KDOC_END

/** */
internal fun ASTNode?.isKDocStart(): Boolean = this != null && elementType == KDOC_START

/** */
internal fun ASTNode?.isFirstAfterKDocStart(): Boolean =
  this != null && prevSibling()?.isKDocStart() == true

/** */
internal fun ASTNode?.isKDocCodeBlockText(): Boolean =
  this != null && elementType == KDOC_CODE_BLOCK_TEXT

/**
 * The opening backticks with or without a language.
 *
 * @since 1.0.1
 */
internal fun ASTNode.isKDocCodeBlockStartText(): Boolean {
  if (elementType != KDOC_TEXT) return false

  return nextSibling { !it.isWhiteSpace() && !it.isKDocLeadingAsterisk() }
    .isKDocCodeBlockText()
}

/**
 * The closing backticks.
 *
 * @since 1.0.1
 */
internal fun ASTNode.isKDocCodeBlockEndText(): Boolean {
  if (elementType != KDOC_TEXT) return false

  return prevSibling { !it.isWhiteSpace() && !it.isKDocLeadingAsterisk() }
    .isKDocCodeBlockText()
}

/** */
internal fun ASTNode.childrenDepthFirst(): Sequence<ASTNode> {
  return depthFirstTraversal { children().toList() }
}

/** */
internal fun ASTNode.childrenDepthFirst(
  predicate: (ASTNode) -> Boolean
): Sequence<ASTNode> = depthFirstTraversal {
  children()
    .filter(predicate)
    .toList()
}

/** */
internal fun ASTNode.childrenBreadthFirst(): Sequence<ASTNode> {
  return breadthFirstTraversal { children().toList() }
}

/** */
internal fun ASTNode.childrenBreadthFirst(
  predicate: (ASTNode) -> Boolean
): Sequence<ASTNode> = breadthFirstTraversal {
  children()
    .filter(predicate)
    .toList()
}
