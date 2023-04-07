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
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevSibling
import com.rickbusarow.ktrules.rules.internal.trees.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

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

/** */
internal fun ASTNode?.isFile(): Boolean = this?.elementType == ElementType.FILE

/** */
internal fun ASTNode?.isTopLevel(): Boolean = this?.parent.isFile()

/** */
internal fun ASTNode?.isScript(): Boolean = this?.parent?.elementType == ElementType.SCRIPT

/** @since 1.0.4 */
internal val ASTNode.parent: ASTNode? get() = treeParent

/** @since 1.0.4 */
internal fun ASTNode.isFirstChild(): Boolean = prevSibling() == null

/** @since 1.0.4 */
internal fun ASTNode.prevSibling(): ASTNode? = prevSibling { true }

/** @since 1.0.4 */
internal fun ASTNode.nextSibling(): ASTNode? = nextSibling { true }

/** @since 1.0.4 */
internal fun ASTNode.childrenDepthFirst(): Sequence<ASTNode> {
  return depthFirstTraversal { children().toList() }
}

/** */
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
