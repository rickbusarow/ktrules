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

package com.rickbusarow.ktrules.rules.internal.markdown

import com.rickbusarow.ktrules.rules.internal.checkNotNull
import com.rickbusarow.ktrules.rules.internal.trees.Traversals.depthFirstTraversal
import org.intellij.lang.annotations.Language
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolder
import kotlin.LazyThreadSafetyMode.NONE

/** @since 1.0.4 */
internal data class MarkdownNode(
  val node: ASTNode,
  private val fullText: String,
  val parent: MarkdownNode?
) : UserDataHolder {
  val text: String by lazy { node.getTextInNode(fullText).toString() }

  val isLeaf: Boolean get() = node.children.isEmpty()
  val isParagraph: Boolean get() = node.type == MarkdownElementTypes.PARAGRAPH
  val isLeafOrParagraph: Boolean get() = isLeaf || isParagraph
  val children: List<MarkdownNode> by lazy {
    node.children
      .map { child ->
        MarkdownNode(
          node = child,
          fullText = fullText,
          parent = this@MarkdownNode
        )
      }
  }

  val isTopLevelInFile: Boolean by lazy(NONE) {
    parent?.elementType == MarkdownElementTypes.MARKDOWN_FILE
  }
  val isParagraphInListItem: Boolean by lazy(NONE) {
    isParagraph && parent.isListItem()
  }

  val textLength by lazy(NONE) { text.length }
  val textLengthWithoutWhitespace by lazy(NONE) { text.count { !it.isWhitespace() } }

  val listItemDelimiterUnsafe: MarkdownNode by lazy(NONE) {
    when (elementType) {
      MarkdownElementTypes.LIST_ITEM -> children.first()
        .also {
          require(it.isListItemDelimiter()) {
            "Expected a list item delimiter but the type is ${it.elementType}"
          }
        }

      MarkdownTokenTypes.LIST_NUMBER -> this
      MarkdownTokenTypes.LIST_BULLET -> this
      // MarkdownElementTypes.PARAGRAPH -> parent

      else ->
        parent
          .checkNotNull { "The parent node is null." }
          // .check({ it.isListItemDelimiter() }) {
          //   "Expected a list item delimiter but the type is ${it.elementType}"
          // }
          .listItemDelimiterUnsafe
    }
  }

  val elementType: IElementType get() = node.type

  private val userData = mutableMapOf<Key<*>, Any?>()
  override fun <T : Any?> getUserData(key: Key<T>): T? {
    @Suppress("UNCHECKED_CAST")
    return userData[key] as? T
  }

  override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    userData[key] = value
  }

  companion object {
    fun from(
      @Language("markdown") markdown: String,
      flavourDescriptor: MarkdownFlavourDescriptor
    ): MarkdownNode {
      return from(markdown, MarkdownParser(flavourDescriptor))
    }

    fun from(
      @Language("markdown") markdown: String,
      markdownParser: MarkdownParser = MarkdownParser(GFMFlavourDescriptor())
    ): MarkdownNode = MarkdownNode(
      node = markdownParser.buildMarkdownTreeFromString(markdown),
      fullText = markdown,
      parent = null
    )
  }
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isEOL(): Boolean = this != null && elementType == MarkdownTokenTypes.EOL

/** @since 1.0.4 */
internal fun MarkdownNode.previousSibling(): MarkdownNode? {
  val currentParent = this.parent ?: return null
  val siblings = currentParent.children
  val index = siblings.indexOf(this)

  return if (index > 0) siblings[index - 1] else null
}

/** @since 1.0.4 */
internal fun MarkdownNode.previousSiblings(): Sequence<MarkdownNode> {
  return generateSequence(previousSibling()) { it.previousSibling() }
}

/** @since 1.0.4 */
internal fun MarkdownNode.previousLeaf(): MarkdownNode? {
  return generateSequence(previousSibling()) { it.children.lastOrNull() }
    .firstOrNull { it.isLeaf }
    ?: parent?.previousLeaf()
}

/** @since 1.0.4 */
internal fun MarkdownNode.previousLeaves(): Sequence<MarkdownNode> {
  return previousSiblings().filter { it.isLeaf }
}

/** @since 1.0.4 */
internal fun MarkdownNode.nextSibling(): MarkdownNode? {

  val currentParent = this.parent ?: return null
  val siblings = currentParent.children
  val index = siblings.indexOf(this)

  return if (index < siblings.size - 1) siblings[index + 1] else null
}

/** @since 1.0.4 */
internal fun MarkdownNode.nextSiblings(): Sequence<MarkdownNode> {
  return generateSequence(nextSibling()) { it.nextSibling() }
}

/** @since 1.0.4 */
internal fun MarkdownNode.nextLeaf(): MarkdownNode? {
  return generateSequence(nextSibling()) { it.children.firstOrNull() }
    .firstOrNull { it.isLeaf }
    ?: parent?.nextLeaf()
}

/** @since 1.0.4 */
internal fun MarkdownNode.nextLeaves(): Sequence<MarkdownNode> {
  return nextSiblings().filter { it.isLeaf }
}

/** @since 1.0.4 */
internal fun MarkdownNode.parents(): Sequence<MarkdownNode> = generateSequence(parent) { it.parent }

/** @since 1.0.4 */
internal fun MarkdownNode.parentsWithSelf(): Sequence<MarkdownNode> =
  generateSequence(this) { it.parent }

/** @since 1.0.4 */
internal fun MarkdownNode.parentListItems() = parents()
  .filter { it.elementType == MarkdownElementTypes.LIST_ITEM }

/** @since 1.0.4 */
internal fun MarkdownNode.parentListItemsWithSelf() = parentsWithSelf()
  .filter { it.elementType == MarkdownElementTypes.LIST_ITEM }

/** @since 1.0.4 */
internal fun MarkdownNode.countParentLists() = parentListItems().count()

/** @since 1.0.4 */
internal fun MarkdownNode.countParentListsWithSelf() = parentListItemsWithSelf().count()

/** @since 1.0.4 */
internal fun MarkdownNode?.isUnorderedList(): Boolean {
  return this != null && elementType == MarkdownElementTypes.UNORDERED_LIST
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isOrderedList(): Boolean {
  return this != null && elementType == MarkdownElementTypes.ORDERED_LIST
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isList(): Boolean {
  return isUnorderedList() || isOrderedList()
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isListItem(): Boolean {
  return this != null && elementType == MarkdownElementTypes.LIST_ITEM
}

/** @since 1.0.4 */
internal fun MarkdownNode.isParagraphInBlockQuote(): Boolean {
  return isParagraph && parent.isBlockQuoteElement()
}

/** @since 1.0.4 */
internal fun MarkdownNode.isFirstParagraphInParent(): Boolean {
  return isParagraph && previousSiblings().none { it.isParagraph }
}

/** @since 1.0.4 */
internal fun MarkdownNode.isListItemDelimiter(): Boolean {
  return elementType == MarkdownTokenTypes.LIST_BULLET ||
    elementType == MarkdownTokenTypes.LIST_NUMBER
}

/** @since 1.0.4 */
internal fun MarkdownNode.countParentBlockQuotes(): Int {
  return parents().count { it.isBlockQuoteElement() }
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isBlockQuoteElement(): Boolean {
  if (this == null) return false
  return elementType == MarkdownElementTypes.BLOCK_QUOTE
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isBlockQuoteToken(): Boolean {
  if (this == null) return false
  return elementType == MarkdownTokenTypes.BLOCK_QUOTE
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isBlockQuoteTokenOrBlockQuoteTokenInWhiteSpace(): Boolean {
  return isBlockQuoteToken() || isBlockQuoteTokenInWhiteSpace()
}

/**
 * When a block quote is nested, any angle brackets other than the last one are
 * incorrectly parsed as WHITE_SPACE, and added as the last node of the parent block quote.
 *
 * @since 1.0.4
 */
internal fun MarkdownNode?.isBlockQuoteTokenInWhiteSpace(): Boolean {
  return this != null &&
    elementType == MarkdownTokenTypes.WHITE_SPACE &&
    text.matches("^>\\s&&[^\\n]+\$".toRegex())
}

/** @since 1.0.4 */
internal fun MarkdownNode.isWhiteSpace(): Boolean {
  return elementType == MarkdownTokenTypes.WHITE_SPACE
}

/** @since 1.0.4 */
internal fun MarkdownNode.isWhiteSpaceAfterNewLine(): Boolean {
  return isWhiteSpace() && previousSibling()?.elementType == MarkdownTokenTypes.EOL
}

/** @since 1.0.4 */
internal fun MarkdownNode?.isEolFollowedByParagraph(): Boolean {
  return when {
    this == null -> false
    !isEOL() -> false
    else -> nextSiblings().any { it.isParagraph }
  }
}

/** @since 1.0.4 */
internal fun MarkdownNode.firstChildOfTypeOrNull(vararg types: IElementType): MarkdownNode? {
  return childrenDepthFirst()
    .firstOrNull { it.elementType in types }
}

/** @since 1.0.4 */
internal fun MarkdownNode.firstChildOfType(vararg types: IElementType): MarkdownNode {
  return childrenDepthFirst()
    .first { it.elementType in types }
}

/** @since 1.0.4 */
internal fun MarkdownNode.childrenDepthFirst(): Sequence<MarkdownNode> {
  return depthFirstTraversal(this) { children }
}

/** @since 1.0.4 */
internal inline fun MarkdownNode.childrenDepthFirst(
  crossinline predicate: (MarkdownNode) -> Boolean
): Sequence<MarkdownNode> = depthFirstTraversal(this) { children.filter(predicate) }
