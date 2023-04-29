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
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

/**
 * @return a sequence of the children of this [ASTNode].
 * @since 1.1.0
 */
fun ASTNode.children(): Sequence<ASTNode> =
  generateSequence(firstChildNode) { node -> node.treeNext }

/**
 * Finds the previous leaf [ASTNode] relative to the receiver, optionally including empty nodes.
 *
 * @param includeEmpty If true, includes empty nodes in the search; otherwise, skips them.
 * @return The previous leaf [ASTNode] or null if there is none.
 * @since 1.1.0
 */
fun ASTNode.prevLeaf(includeEmpty: Boolean = false): ASTNode? {
  var n = this.prevLeafAny()
  if (!includeEmpty) {
    while (n != null && n.textLength == 0) {
      n = n.prevLeafAny()
    }
  }
  return n
}

/**
 * Finds the previous leaf [ASTNode] relative to the receiver that satisfies the given predicate
 * [p].
 *
 * @param p The predicate that the resulting [ASTNode] must satisfy.
 * @return The previous leaf [ASTNode] or null if there is none.
 * @since 1.1.0
 */
fun ASTNode.prevLeaf(p: (ASTNode) -> Boolean): ASTNode? {
  var n = this.prevLeafAny()
  while (n != null && !p(n)) {
    n = n.prevLeafAny()
  }
  return n
}

/**
 * Finds the previous leaf [ASTNode] relative to the receiver without any conditions.
 *
 * @return The previous leaf [ASTNode] or null if there is none.
 * @since 1.1.0
 */
fun ASTNode.prevLeafAny(): ASTNode? {
  val prevSibling = treePrev
  if (prevSibling != null) {
    return treePrev.lastChildLeafOrSelf()
  }
  return treeParent?.prevLeafAny()
}

/**
 * Finds the last child leaf of this [ASTNode] or the node itself if it has no children.
 *
 * @return The last child leaf [ASTNode] or the node itself.
 * @since 1.1.0
 */
fun ASTNode.lastChildLeafOrSelf(): ASTNode {
  return childrenDepthFirst().lastOrNull { it.isLeaf() } ?: this
}

/**
 * Finds the previous code leaf [ASTNode] relative to the receiver, optionally including empty
 * nodes.
 *
 * @param includeEmpty If true, includes empty nodes in the search; otherwise, skips them.
 * @return The previous code leaf [ASTNode] or null if there is none.
 * @since 1.1.0
 */
fun ASTNode.prevCodeLeaf(includeEmpty: Boolean = false): ASTNode? {
  var n = prevLeaf(includeEmpty)
  while (n != null && (n.elementType == ElementType.WHITE_SPACE || n.isPartOfComment())) {
    n = n.prevLeaf(includeEmpty)
  }
  return n
}

/**
 * @return the previous code sibling [ASTNode] by filtering out white spaces and comments.
 * @since 1.1.0
 */
fun ASTNode.prevCodeSibling(): ASTNode? =
  prevSibling { it.elementType != ElementType.WHITE_SPACE && !it.isPartOfComment() }

/**
 * @return the previous sibling [ASTNode] that satisfies the given [predicate].
 * @since 1.1.0
 */
inline fun ASTNode.prevSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? {
  var n = this.treePrev
  while (n != null) {
    if (predicate(n)) {
      return n
    }
    n = n.treePrev
  }
  return null
}

/**
 * @param includeEmpty If true, includes empty leaf nodes in the search.
 * @param skipSubtree If true, skips the subtree during the search.
 * @return the next code leaf [ASTNode] by filtering out white spaces and comments.
 * @since 1.1.0
 */
fun ASTNode.nextCodeLeaf(
  includeEmpty: Boolean = false,
  skipSubtree: Boolean = false,
): ASTNode? {
  var n = nextLeaf(includeEmpty, skipSubtree)
  while (n != null && (n.elementType == ElementType.WHITE_SPACE || n.isPartOfComment())) {
    n = n.nextLeaf(includeEmpty, skipSubtree)
  }
  return n
}

/**
 * @return the next code sibling [ASTNode] by filtering out white spaces and comments.
 * @since 1.1.0
 */
fun ASTNode.nextCodeSibling(): ASTNode? =
  nextSibling { it.elementType != ElementType.WHITE_SPACE && !it.isPartOfComment() }

/**
 * @return the next sibling [ASTNode] that satisfies the given [predicate].
 * @since 1.1.0
 */
inline fun ASTNode.nextSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? {
  var n = this.treeNext
  while (n != null) {
    if (predicate(n)) {
      return n
    }
    n = n.treeNext
  }
  return null
}

/**
 * @param strict If true, the search starts from the receiver node's parent; otherwise, it starts
 *   from the receiver node.
 * @param predicate returns true for the matching predicate
 * @return the parent [ASTNode] that satisfies the given [predicate].
 * @since 1.1.0
 */
fun ASTNode.parent(
  strict: Boolean = true,
  predicate: (ASTNode) -> Boolean,
): ASTNode? {
  var n: ASTNode? = if (strict) this.treeParent else this
  while (n != null) {
    if (predicate(n)) {
      return n
    }
    n = n.treeParent
  }
  return null
}

/**
 * @param elementType the requested type of node
 * @param strict If true, the search starts from the receiver node's parent; otherwise, it starts
 *   from the receiver node.
 * @return the parent [ASTNode] with the given [elementType].
 * @since 1.1.0
 */
fun ASTNode.parent(
  elementType: IElementType,
  strict: Boolean = true,
): ASTNode? {
  var n: ASTNode? = if (strict) this.treeParent else this
  while (n != null) {
    if (n.elementType == elementType) {
      return n
    }
    n = n.treeParent
  }
  return null
}

/**
 * @param includeEmpty If true, includes empty leaf nodes in the search.
 * @param skipSubtree If true, skips the subtree during the search.
 * @return the next leaf [ASTNode] based on the given parameters.
 * @since 1.1.0
 */
fun ASTNode.nextLeaf(
  includeEmpty: Boolean = false,
  skipSubtree: Boolean = false,
): ASTNode? {
  var n = if (skipSubtree) this.lastChildLeafOrSelf().nextLeafAny() else this.nextLeafAny()
  if (!includeEmpty) {
    while (n != null && n.textLength == 0) {
      n = n.nextLeafAny()
    }
  }
  return n
}

/**
 * @return the next leaf [ASTNode] that satisfies the given predicate [p].
 * @since 1.1.0
 */
fun ASTNode.nextLeaf(p: (ASTNode) -> Boolean): ASTNode? {
  var n = this.nextLeafAny()
  while (n != null && !p(n)) {
    n = n.nextLeafAny()
  }
  return n
}

/**
 * @return the next leaf [ASTNode] without any specific conditions.
 * @since 1.1.0
 */
fun ASTNode.nextLeafAny(): ASTNode? {
  var n = this
  if (n.firstChildNode != null) {
    do {
      n = n.firstChildNode
    } while (n.firstChildNode != null)
    return n
  }
  return n.nextLeafStrict()
}

/**
 * @return the next leaf [ASTNode] by traversing the tree strictly.
 * @since 1.1.0
 */
fun ASTNode.nextLeafStrict(): ASTNode? {
  val nextSibling: ASTNode? = treeNext
  if (nextSibling != null) {
    return nextSibling.firstChildLeafOrSelf()
  }
  return treeParent?.nextLeafStrict()
}

/**
 * @return the first child leaf [ASTNode] or the receiver node if it has no children.
 * @since 1.1.0
 */
fun ASTNode.firstChildLeafOrSelf(): ASTNode {
  var n = this
  if (n.firstChildNode != null) {
    do {
      n = n.firstChildNode
    } while (n.firstChildNode != null)
    return n
  }
  return n
}

/**
 * Check if the given [ASTNode] is a code leaf. E.g. it must be a leaf and may not be a whitespace
 * or be part of a comment. @return true if the receiver node is a leaf, not a whitespace, and not
 * part of a comment
 *
 * @since 1.1.0
 */
fun ASTNode.isCodeLeaf(): Boolean = isLeaf() && !isWhiteSpace() && !isPartOfComment()

/**
 * @return true if the node is part of a KDoc comment, block comment, or end-of-line comment
 * @since 1.1.0
 */
fun ASTNode.isPartOfComment(): Boolean = parent(strict = false) { it.psi is PsiComment } != null

/**
 * Updates or inserts a new whitespace element with [text] before the given node. If the node itself
 * is a whitespace then its contents is replaced with [text]. If the node is a (nested) composite
 * element, the whitespace element is added after the previous leaf node.
 *
 * @since 1.1.0
 */
fun ASTNode.upsertWhitespaceBeforeMe(text: String) {
  if (this is LeafElement) {
    if (this.elementType == ElementType.WHITE_SPACE) {
      return replaceWhitespaceWith(text)
    }
    val previous = treePrev ?: prevLeaf()
    if (previous != null && previous.elementType == ElementType.WHITE_SPACE) {
      previous.replaceWhitespaceWith(text)
    } else {
      PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
        (psi as LeafElement).rawInsertBeforeMe(psiWhiteSpace)
      }
    }
  } else {
    val prevLeaf =
      requireNotNull(prevLeaf()) {
        "Can not upsert a whitespace if the first node is a non-leaf node"
      }
    prevLeaf.upsertWhitespaceAfterMe(text)
  }
}

/**
 * Updates or inserts a new whitespace element with [text] after the given node. If the node itself
 * is a whitespace then its contents is replaced with [text]. If the node is a (nested) composite
 * element, the whitespace element is added after the last child leaf.
 *
 * @since 1.1.0
 */
fun ASTNode.upsertWhitespaceAfterMe(text: String) {
  if (this is LeafElement) {
    if (this.elementType == ElementType.WHITE_SPACE) {
      return replaceWhitespaceWith(text)
    }
    val next = treeNext ?: nextLeaf()
    if (next != null && next.elementType == ElementType.WHITE_SPACE) {
      next.replaceWhitespaceWith(text)
    } else {
      PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
        (psi as LeafElement).rawInsertAfterMe(psiWhiteSpace)
      }
    }
  } else {
    lastChildLeafOrSelf().upsertWhitespaceAfterMe(text)
  }
}

/**
 * Replaces the receiver [ASTNode] white space with the given [text].
 *
 * @since 1.1.0
 * @throws IllegalArgumentException if the receiver [ASTNode] does not have a WHITE_SPACE element
 *   type.
 */
fun ASTNode.replaceWhitespaceWith(text: String) {
  require(this.elementType == ElementType.WHITE_SPACE)
  if (this.text != text) {
    (this.psi as LeafElement).rawReplaceWithText(text)
  }
}

/**
 * @return the column number of the receiver [ASTNode].
 * @since 1.1.0
 */
val ASTNode.column: Int
  get() {
    var leaf = this.prevLeaf()
    var offsetToTheLeft = 0
    while (leaf != null) {
      if (leaf.isWhiteSpaceWithNewline() || leaf.isRegularStringWithNewline()) {
        offsetToTheLeft += leaf.textLength - 1 - leaf.text.lastIndexOf('\n')
      } else {
        offsetToTheLeft += leaf.textLength
        leaf = leaf.prevLeaf()
      }
    }
    return offsetToTheLeft + 1
  }

/**
 * @return true if the [ASTNode] is part of a string, false otherwise.
 * @since 1.1.0
 */
fun ASTNode.isPartOfString(): Boolean = parent(ElementType.STRING_TEMPLATE, strict = false) != null

/**
 * @return true if the [ASTNode] is white space, false otherwise.
 * @since 1.1.0
 */
fun ASTNode?.isWhiteSpace(): Boolean = this != null && elementType == ElementType.WHITE_SPACE

/**
 * @return true if the [ASTNode] is white space with a newline, false otherwise.
 * @since 1.1.0
 */
fun ASTNode?.isWhiteSpaceWithNewline(): Boolean =
  this != null && elementType == ElementType.WHITE_SPACE && textContains('\n')

/**
 * @return true if the [ASTNode] is white space with a newline, false otherwise.
 * @since 1.1.0
 */
fun ASTNode?.isRegularStringWithNewline(): Boolean =
  this != null && elementType == ElementType.REGULAR_STRING_PART && textContains('\n')

/**
 * @return true if the [ASTNode] is white space without a newline, false otherwise.
 * @since 1.1.0
 */
fun ASTNode?.isWhiteSpaceWithoutNewline(): Boolean =
  this != null && elementType == ElementType.WHITE_SPACE && !textContains('\n')

/**
 * @return true if the [ASTNode] is a root, false otherwise.
 * @since 1.1.0
 */
fun ASTNode.isRoot(): Boolean = elementType == ElementType.FILE

/**
 * @return true if the [ASTNode] is a leaf (has no children), false otherwise.
 * @since 1.1.0
 */
fun ASTNode.isLeaf(): Boolean = firstChildNode == null
