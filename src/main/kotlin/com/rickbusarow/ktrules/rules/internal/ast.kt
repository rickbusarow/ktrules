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

package com.rickbusarow.ktrules.rules.internal

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC_CODE_BLOCK_TEXT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_START
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import java.util.LinkedList

internal fun ASTNode.isBlank() = text.isBlank()

private val copyRightCommentStart = Regex(
  """(?:\/\*{1,2}\s+(?:\*\s)?|\/\/ *)Copyright [\s\S]*"""
)

internal fun ASTNode.isCopyrightHeader(): Boolean {
  if (elementType != ElementType.BLOCK_COMMENT) return false

  return text.matches(copyRightCommentStart)
}

internal val ASTNode.parent: ASTNode? get() = treeParent
internal fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }
internal fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }
internal fun ASTNode?.isKDocText(): Boolean = this != null && elementType == KDOC_TEXT
internal fun ASTNode?.isKDocTag(): Boolean = this != null && elementType == KDOC_TAG

internal fun ASTNode?.isWhitespaceAfterKDocLeadingAsterisk(): Boolean =
  this != null && elementType == WHITE_SPACE && prevSibling().isKDocLeadingAsterisk()

internal fun ASTNode?.isWhitespaceBeforeKDocLeadingAsterisk(): Boolean =
  this != null && elementType == WHITE_SPACE && nextSibling().isKDocLeadingAsterisk()

internal fun ASTNode?.isKDocLeadingAsterisk(): Boolean =
  this != null && elementType == KDOC_LEADING_ASTERISK

internal fun ASTNode?.isKDocEnd(): Boolean = this != null && elementType == KDOC_END
internal fun ASTNode?.isKDocStart(): Boolean = this != null && elementType == KDOC_START
internal fun ASTNode?.isFirstAfterKDocStart(): Boolean =
  this != null && prevSibling()?.isKDocStart() == true

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

internal fun <T> Sequence<T>.stateful(): Sequence<T> {
  val iterator = iterator()
  return Sequence { iterator }
}

internal fun ASTNode.depthFirst(): Sequence<ASTNode> {

  val toVisit = LinkedList(children().toList())

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    val childrenList = node.children().toList()

    val lastIndex = childrenList.lastIndex

    repeat(lastIndex + 1) {
      toVisit.addFirst(childrenList[lastIndex - it])
    }

    toVisit.removeFirstOrNull()
  }
}
