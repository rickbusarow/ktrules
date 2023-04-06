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

/** @since 1.0.4 */
internal fun ASTNode?.isBlank(): Boolean = this != null && text.isBlank()

/** @since 1.0.4 */
private val copyRightCommentStart = Regex(
  """(?:\/\*{1,2}\s+(?:\*\s)?|\/\/ *)Copyright [\s\S]*"""
)

/** @since 1.0.4 */
internal fun ASTNode.isCopyrightHeader(): Boolean {
  if (elementType != ElementType.BLOCK_COMMENT) return false

  return text.matches(copyRightCommentStart)
}

/** @since 1.0.4 */
internal val ASTNode.parent: ASTNode? get() = treeParent

/** @since 1.0.4 */
internal fun ASTNode.isFirstChild(): Boolean = prevSibling() == null

/** @since 1.0.4 */
internal fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }

/** @since 1.0.4 */
internal fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }

/** @since 1.0.4 */
internal fun ASTNode?.isKDocText(): Boolean = this != null && elementType == KDOC_TEXT

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTag(): Boolean = this != null && elementType == KDOC_TAG

/** @since 1.0.4 */
internal fun ASTNode?.isKDocSectionWithTagChildren(): Boolean =
  this != null && elementType == KDOC_SECTION && children().any { it.isKDocTag() }

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagWithTagChildren(): Boolean =
  this != null && elementType == KDOC_TAG && children().any { it.isKDocTag() }

/** @since 1.0.4 */
internal fun ASTNode?.isKDocSection(): Boolean = this != null && elementType == KDOC_SECTION

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagOrSection(): Boolean = isKDocSection() || isKDocTag()

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagName(): Boolean = this != null && elementType == KDOC_TAG_NAME

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagLink(): Boolean {
  return this != null && elementType == IDENTIFIER && parent?.elementType == KDOC_NAME
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocMarkdownLink(): Boolean =
  this != null && elementType == KDOC_MARKDOWN_LINK

/** @since 1.0.4 */
internal fun ASTNode?.isKDocDefaultSection(): Boolean = this?.psi?.isKDocDefaultSection() == true

/** @since 1.0.4 */
internal fun ASTNode?.isKDocFirstSectionAfterDefault(): Boolean {
  return when {
    this == null -> false
    isKDocTag() -> parent?.prevSibling().isKDocDefaultSection()
    isKDocSection() -> prevSibling().isKDocDefaultSection()
    else -> false
  }
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocLastSection(): Boolean {

  return when {
    this == null -> false
    isKDocTag() -> parent?.nextSibling().isKDocDefaultSection()
    isKDocSection() -> nextSibling().isKDocDefaultSection()
    else -> false
  }
}

/** @since 1.0.4 */
internal fun ASTNode?.getKDocSection(): ASTNode? {
  return this?.parents()?.firstOrNull { it.isKDocSection() }
}

/** @since 1.0.4 */
internal fun ASTNode?.isInKDocTag(): Boolean {
  return this != null && parents().any { it.isKDocTag() }
}

/** @since 1.0.4 */
internal fun ASTNode?.isInKDocDefaultSection(): Boolean {
  if (this == null) return false

  val kdoc = psi.getNonStrictParentOfType<KDoc>() ?: return false

  val defaultSection = kdoc.getDefaultSection().node

  return this == defaultSection || parents().any { it == defaultSection }
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocWhitespaceAfterLeadingAsterisk(): Boolean =
  this != null && (isWhiteSpace() || isBlank()) && prevSibling().isKDocLeadingAsterisk()

/** @since 1.0.4 */
internal fun ASTNode?.isKDocWhitespaceBeforeLeadingAsterisk(): Boolean =
  this != null && elementType == WHITE_SPACE && nextSibling().isKDocLeadingAsterisk()

/** @since 1.0.4 */
internal fun ASTNode?.isKDocLeadingAsterisk(): Boolean =
  this != null && elementType == KDOC_LEADING_ASTERISK

/** @since 1.0.4 */
internal fun ASTNode?.isKDocEnd(): Boolean = this != null && elementType == KDOC_END

/** @since 1.0.4 */
internal fun ASTNode?.isKDocStart(): Boolean = this != null && elementType == KDOC_START

/** @since 1.0.4 */
internal fun ASTNode?.isFirstAfterKDocStart(): Boolean =
  this != null && prevSibling()?.isKDocStart() == true

/** @since 1.0.4 */
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

/** @since 1.0.4 */
internal fun ASTNode.childrenDepthFirst(): Sequence<ASTNode> {
  return depthFirstTraversal { children().toList() }
}

/** @since 1.0.4 */
internal fun ASTNode.childrenDepthFirst(
  predicate: (ASTNode) -> Boolean
): Sequence<ASTNode> = depthFirstTraversal {
  children()
    .filter(predicate)
    .toList()
}

/** @since 1.0.4 */
internal fun ASTNode.childrenBreadthFirst(): Sequence<ASTNode> {
  return breadthFirstTraversal { children().toList() }
}

/** @since 1.0.4 */
internal fun ASTNode.childrenBreadthFirst(
  predicate: (ASTNode) -> Boolean
): Sequence<ASTNode> = breadthFirstTraversal {
  children()
    .filter(predicate)
    .toList()
}
