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

import org.intellij.markdown.ast.ASTNode
import java.util.LinkedList

internal fun ASTNode.depthFirst(): Sequence<ASTNode> {

  val toVisit = LinkedList(children)

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    repeat(node.children.lastIndex + 1) {
      toVisit.addFirst(node.children[node.children.lastIndex - it])
    }
    toVisit.removeFirstOrNull()
  }
}

internal inline fun ASTNode.depthFirst(
  crossinline predicate: (ASTNode) -> Boolean
): Sequence<ASTNode> {

  val toVisit = LinkedList(children.filter(predicate))

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    if (predicate(node)) {
      val filtered = node.children.filter(predicate)

      repeat(filtered.lastIndex + 1) {
        toVisit.addFirst(filtered[filtered.lastIndex - it])
      }

      toVisit.removeFirstOrNull()
    } else {
      null
    }
  }
}
