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
import com.pinterest.ktlint.core.ast.nextLeaf
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevLeaf
import com.pinterest.ktlint.core.ast.prevSibling
import com.rickbusarow.ktrules.rules.internal.trees.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import kotlin.contracts.contract

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

/** @since 1.0.6 */
internal fun ASTNode?.isFile(): Boolean = this?.elementType == ElementType.FILE

/** @since 1.0.6 */
internal fun ASTNode?.isTopLevel(): Boolean = this?.parent.isFile()

/** @since 1.0.7 */
internal fun ASTNode?.isWhiteSpaceOrBlank(): Boolean {

  contract {
    returns(true) implies (this@isWhiteSpaceOrBlank != null)
  }

  if (this == null) return false
  return isWhiteSpace() || isBlank()
}

/** @since 1.0.6 */
internal fun ASTNode?.isScript(): Boolean = this?.parent?.elementType == ElementType.SCRIPT

/** @since 1.0.4 */
internal val ASTNode.parent: ASTNode? get() = treeParent

/** @since 1.0.4 */
internal fun ASTNode.isFirstChild(): Boolean = prevSibling() == null

/** @since 1.0.4 */
internal fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }

/** @since 1.0.7 */
internal fun ASTNode.prevSiblings(): Sequence<ASTNode> =
  generateSequence(prevSibling()) { it.prevSibling() }

/** @since 1.0.7 */
internal fun ASTNode.prevLeaves(includeEmpty: Boolean = true): Sequence<ASTNode> =
  generateSequence(prevLeaf(includeEmpty = includeEmpty)) { it.prevLeaf(includeEmpty = includeEmpty) }

/** @since 1.0.4 */
internal fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }

/** @since 1.0.7 */
internal fun ASTNode.nextSiblings(): Sequence<ASTNode> =
  generateSequence(nextSibling()) { it.nextSibling() }

/**
 *     this might be a code block 
 *
 * @since 1.0.7
 */
internal fun ASTNode.nextLeaves(includeEmpty: Boolean = true): Sequence<ASTNode> =
  generateSequence(nextLeaf(includeEmpty = includeEmpty)) { it.nextLeaf(includeEmpty = includeEmpty) }

/** @since 1.0.4 */
internal fun ASTNode.childrenDepthFirst(): Sequence<ASTNode> {
  return depthFirstTraversal { children().toList() }
}

/** @since 1.0.6 */
internal fun ASTNode.parentsWithSelf(): Sequence<ASTNode> {
  return generateSequence(this) { it.parent }
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

/** @since 1.0.7 */
internal fun ASTNode.fileIndent(additionalOffset: Int): String {
  return psi.fileIndent(additionalOffset = additionalOffset)
}

/** @since 1.0.7 */
internal fun ASTNode.removeAllChildren(shouldRemove: (ASTNode) -> Boolean = { true }) {
  children()
    .toList()
    .filter(shouldRemove)
    .forEach { removeChild(it) }
}

/** @since 1.0.7 */
internal fun ASTNode.removeAllChildrenRecursive(shouldRemove: (ASTNode) -> Boolean) {
  removeAllChildren(shouldRemove)
  children()
    .toList()
    .forEach { it.removeAllChildrenRecursive(shouldRemove) }
}
