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
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevLeaf
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/** @since 1.0.5 */
internal val KDocKnownTag.Companion.AT_PARAM get() = "@param"

/** @since 1.0.5 */
internal val KDocKnownTag.Companion.AT_PROPERTY get() = "@property"

/** @since 1.0.4 */
internal fun ASTNode?.isKDocText(): Boolean = this != null && elementType == ElementType.KDOC_TEXT

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTag(): Boolean = this != null && elementType == ElementType.KDOC_TAG

/** @since 1.0.4 */
internal fun ASTNode?.isKDocSectionWithTagChildren(): Boolean =
  this != null && elementType == ElementType.KDOC_SECTION && children().any { it.isKDocTag() }

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagWithTagChildren(): Boolean =
  this != null && elementType == ElementType.KDOC_TAG && children().any { it.isKDocTag() }

/** @since 1.0.4 */
internal fun ASTNode?.isKDocSection(): Boolean =
  this != null && elementType == ElementType.KDOC_SECTION

/** @since 1.0.4 */
internal fun ASTNode?.isKDocTagOrSection(): Boolean = isKDocSection() || isKDocTag()

/**
 * @return true if this is a KDoc tag "type" name, like `@param`, `@property`, `@throws`, etc.
 * @since 1.0.5
 */
internal fun ASTNode?.isKDocTagName(): Boolean {
  return this != null && elementType == ElementType.KDOC_TAG_NAME
}

/**
 * @return true if this is a KDoc tag identifier/link, like `myParameter` from
 *   `@param myParameter [...]`
 * @since 1.0.5
 **/
internal fun ASTNode?.isKDocTagMarkdownLink(): Boolean {
  return isKDocMarkdownLink() && this?.parent.isKDocTag()
}

/**
 * @return true if this is a KDoc tag identifier/link name, like `myParameter` from
 *   `@param myParameter [...]`
 * @since 1.0.5
 **/
internal fun ASTNode?.isKDocTagLinkName(): Boolean {
  return this != null && elementType == ElementType.KDOC_NAME && parent.isKDocTagMarkdownLink()
}

/**
 * @return true if this is a KDoc tag identifier/link name identifier, like `myParameter` from
 *   `@param myParameter [...]`
 * @since 1.0.5
 **/
internal fun ASTNode?.isKDocTagLinkNameIdentifier(): Boolean {
  return this != null && elementType == ElementType.IDENTIFIER && parent.isKDocTagLinkName()
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocMarkdownLink(): Boolean =
  this != null && elementType == ElementType.KDOC_MARKDOWN_LINK

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

/** */
internal fun ASTNode?.isKDocWhitespaceAfterKDocStart(): Boolean {

  if (!isWhiteSpaceOrBlank()) return false
  return prevLeaf(true).isKDocStart()
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocWhitespaceAfterLeadingAsterisk(): Boolean {

  if (!isWhiteSpaceOrBlank()) return false
  return prevLeaf(true).isKDocLeadingAsterisk()
}

/** @since 1.0.4 */
internal fun ASTNode?.isKDocWhitespaceBeforeLeadingAsterisk(): Boolean =
  this != null && elementType == ElementType.WHITE_SPACE && nextSibling().isKDocLeadingAsterisk()

/** @since 1.0.4 */
internal fun ASTNode?.isKDocLeadingAsterisk(): Boolean =
  this != null && elementType == ElementType.KDOC_LEADING_ASTERISK

/** @since 1.0.4 */
internal fun ASTNode?.isKDocEnd(): Boolean = this != null && elementType == ElementType.KDOC_END

/** @since 1.0.4 */
internal fun ASTNode?.isKDocStart(): Boolean = this != null && elementType == ElementType.KDOC_START

/** */
internal fun ASTNode?.isKDoc(): Boolean = this != null && elementType == ElementType.KDOC

/** @since 1.0.4 */
internal fun ASTNode?.isFirstAfterKDocStart(): Boolean =
  this != null && prevSibling()?.isKDocStart() == true

/** @since 1.0.4 */
internal fun ASTNode?.isKDocCodeBlockText(): Boolean =
  this != null && elementType == ElementType.KDOC_CODE_BLOCK_TEXT

/**
 * @return true if this node is the opening backticks of a code block, with or without a language.
 * @since 1.0.1
 */
internal fun ASTNode.isKDocCodeBlockStartText(): Boolean {
  if (elementType != ElementType.KDOC_TEXT) return false

  return nextSibling { !it.isWhiteSpace() && !it.isKDocLeadingAsterisk() }
    .isKDocCodeBlockText()
}

/**
 * @return true if this node is closing backticks after a code block.
 * @since 1.0.1
 */
internal fun ASTNode.isKDocCodeBlockEndText(): Boolean {
  if (elementType != ElementType.KDOC_TEXT) return false

  return prevSibling { !it.isWhiteSpace() && !it.isKDocLeadingAsterisk() }
    .isKDocCodeBlockText()
}
