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

import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.rules.internal.trees.Traversals.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.Traversals.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import kotlin.contracts.contract

/** @since 1.0.4 */
fun ASTNode?.isBlank(): Boolean = this != null && text.isBlank()

/** @since 1.0.4 */
private val COPYRIGHT_COMMENT_START = Regex(
  """(?:\/\*{1,2}\s+(?:\*\s)?|\/\/ *)Copyright [\s\S]*"""
)

/** @since 1.0.4 */
fun ASTNode.isCopyrightHeader(): Boolean {
  if (elementType != ElementType.BLOCK_COMMENT) return false

  return text.matches(COPYRIGHT_COMMENT_START)
}

/** @since 1.0.6 */
fun ASTNode?.isFile(): Boolean = this?.elementType == ElementType.FILE

/** @since 1.0.6 */
fun ASTNode?.isTopLevel(): Boolean = this?.parent.isFile()

/** @since 1.0.7 */
fun ASTNode?.isWhiteSpaceOrBlank(): Boolean {

  contract {
    returns(true) implies (this@isWhiteSpaceOrBlank != null)
  }

  if (this == null) return false
  return isWhiteSpace() || isBlank()
}

/** @since 1.0.6 */
fun ASTNode?.isScript(): Boolean = this?.parent?.elementType == ElementType.SCRIPT

/** @since 1.0.4 */
val ASTNode.parent: ASTNode? get() = treeParent

/** @since 1.0.4 */
fun ASTNode.isFirstChild(): Boolean = prevSibling() == null

/** @since 1.0.4 */
fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }

/** @since 1.0.7 */
fun ASTNode.prevSiblings(): Sequence<ASTNode> = generateSequence(prevSibling()) { it.prevSibling() }

/** @since 1.0.7 */
fun ASTNode.prevLeaves(includeEmpty: Boolean = true): Sequence<ASTNode> =
  generateSequence(prevLeaf(includeEmpty = includeEmpty)) {
    it.prevLeaf(includeEmpty = includeEmpty)
  }

/** @since 1.0.4 */
fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }

/** @since 1.0.7 */
fun ASTNode.nextSiblings(): Sequence<ASTNode> = generateSequence(nextSibling()) { it.nextSibling() }

/** @since 1.0.7 */
fun ASTNode.nextLeaves(includeEmpty: Boolean = true): Sequence<ASTNode> =
  generateSequence(nextLeaf(includeEmpty = includeEmpty)) {
    it.nextLeaf(includeEmpty = includeEmpty)
  }

/** @since 1.0.4 */
fun ASTNode.childrenDepthFirst(): Sequence<ASTNode> {
  return depthFirstTraversal(this) { children().toList() }
}

/**
 * @return a depth-first [Sequence] of this [ASTNode]'s descendants.
 * @since 1.0.6
 */
fun ASTNode.parentsWithSelf(): Sequence<ASTNode> {
  return generateSequence(this) { it.parent }
}

/**
 * Returns a depth-first [Sequence] of all of this [ASTNode]'s descendants that satisfy the
 * specified [predicate].
 *
 * @param predicate the predicate that each descendant must satisfy to be included in the
 *   [Sequence].
 * @return a depth-first [Sequence] of this [ASTNode]'s descendants that satisfy the [predicate].
 * @since 1.0.4
 */
fun ASTNode.childrenDepthFirst(predicate: (ASTNode) -> Boolean): Sequence<ASTNode> =
  depthFirstTraversal(this) {
    children()
      .filter(predicate)
      .toList()
  }

/**
 * @return a breadth-first [Sequence] of this [ASTNode]'s descendants.
 * @since 1.0.4
 */
fun ASTNode.childrenBreadthFirst(): Sequence<ASTNode> {
  return breadthFirstTraversal(this) { children().toList() }
}

/**
 * Returns a breadth-first [Sequence] of all of this [ASTNode]'s descendants that satisfy the
 * specified [predicate].
 *
 * @param predicate the predicate that each descendant must satisfy to be included in the
 *   [Sequence].
 * @return a breadth-first [Sequence] of this [ASTNode]'s descendants that satisfy the [predicate].
 * @since 1.0.4
 */
fun ASTNode.childrenBreadthFirst(predicate: (ASTNode) -> Boolean): Sequence<ASTNode> =
  breadthFirstTraversal(this) {
    children()
      .filter(predicate)
      .toList()
  }

/** @since 1.0.7 */
fun ASTNode.fileIndent(additionalOffset: Int): String {
  return psi.fileIndent(additionalOffset = additionalOffset)
}

/**
 * @return all ancestors of the receiver node, starting with the immediate parent
 * @since 1.1.0
 */
fun ASTNode.parents(): Sequence<ASTNode> = generateSequence(treeParent) { node -> node.treeParent }

/** @since 1.1.1 */
inline fun <T : ASTNode> T.removeFirstChildrenWhile(shouldRemove: (ASTNode) -> Boolean): T {
  return apply {
    children()
      .toList()
      .takeWhile(shouldRemove)
      .forEach { removeChild(it) }
  }
}

/** @since 1.1.1 */
inline fun <T : ASTNode> T.removeLastChildrenWhile(shouldRemove: (ASTNode) -> Boolean): T {
  return apply {
    children()
      .toList()
      .takeLastWhile(shouldRemove)
      .forEach { removeChild(it) }
  }
}

/** @since 1.0.7 */
inline fun <T : ASTNode> T.removeAllChildren(shouldRemove: (ASTNode) -> Boolean = { true }): T {
  return apply {
    children()
      .toList()
      .filter(shouldRemove)
      .forEach { removeChild(it) }
  }
}

/** @since 1.0.7 */
fun <T : ASTNode> T.removeAllChildrenRecursive(shouldRemove: (ASTNode) -> Boolean): T {
  return apply {
    removeAllChildren(shouldRemove)
    children()
      .toList()
      .forEach { it.removeAllChildrenRecursive(shouldRemove) }
  }
}
