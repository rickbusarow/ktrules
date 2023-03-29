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
import org.intellij.markdown.ast.getTextInNode

internal class MarkdownEverythingPrinter(val fullText: String) {

  private val levels = mutableMapOf<ASTNode, Int>()
  private val dashes = "------------------------------------------------------------"

  private val parentNameMap = mutableMapOf<ASTNode, String>()

  private fun visitRoot(rootNode: ASTNode) {

    rootNode.depthFirst()
      .forEach { node ->

        val thisName = node::class.java.simpleName // + element.extendedTypes()
        val parentName = (node.parentName() ?: "----") + "       ${node.type}"

        val parentLevel = node.parent?.let { parent -> levels[parent] } ?: 0
        levels[node] = parentLevel + 1

        printNode(
          elementSimpleName = thisName,
          parentName = parentName,
          nodeText = node.getTextInNode(fullText).toString(),
          level = parentLevel + 1
        )
      }
  }

  private fun printNode(
    elementSimpleName: String,
    parentName: String,
    nodeText: String,
    level: Int
  ) {
    println(
      """
      |   $dashes  $elementSimpleName    -- parent: $parentName
      |
      |   `$nodeText`
      """.trimMargin()
        .lines()
        .let {
          it.dropLast(1) + it.last().replaceFirst("  ", "└─")
        }
        .joinToString("\n")
        .prependIndent("│   ".repeat(level))
    )
  }

  private fun ASTNode.parentName() = parent?.let { parent ->

    parentNameMap.getOrPut(parent) {
      val typeCount = parentNameMap.keys.count { it::class == parent::class }

      val simpleName = parent::class.java.simpleName

      val start = if (typeCount == 0) {
        simpleName
      } else {
        "$simpleName (${typeCount + 1})"
      }

      start
    }
  }

  companion object {

    internal fun ASTNode.printEverything(fullText: String) = apply {
      MarkdownEverythingPrinter(fullText).visitRoot(this)
    }
  }
}
