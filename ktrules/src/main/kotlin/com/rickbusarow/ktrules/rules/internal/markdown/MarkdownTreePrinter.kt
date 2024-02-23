/*
 * Copyright (C) 2024 Rick Busarow
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

package com.rickbusarow.ktrules.rules.internal.markdown

import com.rickbusarow.ktrules.rules.internal.trees.AbstractTreePrinter

/**
 * prints a tree starting at any markdown tree element,
 * showing all its children types and their text
 *
 * @since 1.0.4
 */
internal class MarkdownTreePrinter(
  whitespaceChar: Char = ' '
) : AbstractTreePrinter<MarkdownNode>(whitespaceChar) {

  override fun MarkdownNode.simpleClassName(): String = this::class.java.simpleName
  override fun MarkdownNode.typeName(): String = elementType.toString()
  override fun MarkdownNode.text(): String = text
  override fun MarkdownNode.children(): Sequence<MarkdownNode> = children.asSequence()

  override fun MarkdownNode.parent(): MarkdownNode? = parent

  companion object {

    /** @since 1.0.4 */
    internal fun MarkdownNode.printEverything(whitespaceChar: Char = ' ') = apply {
      MarkdownTreePrinter(whitespaceChar).printTreeString(this)
    }
  }
}
