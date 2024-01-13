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

import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.prefix
import com.rickbusarow.ktrules.rules.internal.removeRegex
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper
import com.rickbusarow.ktrules.rules.wrapping.StringWrapper.Companion.splitWords
import org.intellij.markdown.MarkdownElementTypes
import kotlin.LazyThreadSafetyMode.NONE

/** @since 1.0.4 */
internal fun MarkdownNode.wrap(
  wrapper: StringWrapper,
  maxLength: Int,
  beforeAnyTags: Boolean,
  addKDocLeadingSpace: Boolean
): String {

  var paragraphCount = 0

  return childrenDepthFirst { it.parent?.elementType != MarkdownElementTypes.PARAGRAPH }
    .filter { it.isLeafOrParagraph || it.isBlockQuoteTokenOrBlockQuoteTokenInWhiteSpace() }
    .joinToString("") { node ->

      if (node.isParagraph) {
        paragraphCount++
      }

      node.getChildText(
        paragraphCount = paragraphCount,
        wrapper = wrapper,
        maxLength = maxLength,
        beforeAnyTags = beforeAnyTags,
        addKDocLeadingSpace = addKDocLeadingSpace
      )
    }
}

/** @since 1.0.4 */
private fun MarkdownNode.getChildText(
  paragraphCount: Int,
  wrapper: StringWrapper,
  maxLength: Int,
  beforeAnyTags: Boolean,
  addKDocLeadingSpace: Boolean
): String {
  val indent = indent(addKDocLeadingSpace, beforeAnyTags)

  return when {
    isParagraph -> {

      val paragraphInBlockQuote = isParagraphInBlockQuote()

      val noTokens = childrenDepthFirst()
        .filter { child ->
          !child.isBlockQuoteTokenOrBlockQuoteTokenInWhiteSpace() || child.nextSibling()
            .isEolFollowedByParagraph()
        }
        .dropWhile { it.isWhiteSpace() && it.text.isBlank() }
        .filter { it.isLeaf }
        .joinToString("") { it.text }
      // .let { block ->
      //   val lastLineIndex = block.count { it == '\n' }
      //   block.mapLinesIndexed { i, line ->
      //     line.letIf(i != lastLineIndex) { trimEnd() }
      //   }
      // }

      val continuationIndent = continuationIndent(
        beforeAnyTags = beforeAnyTags,
        addKDocLeadingSpace = addKDocLeadingSpace
      )

      val remainingMaxLength = when {
        isParagraphInListItem -> maxLength - continuationIndent.length
        paragraphInBlockQuote -> maxLength - indent.length
        else -> maxLength
      }

      wrapper.wrap(
        words = noTokens.splitWords(
          preserveLeadingWhitespace = true,
          preserveTrailingWhitespace = true
        ),
        maxLength = remainingMaxLength,
        leadingIndent = indent,
        continuationIndent = continuationIndent
      )
    }

    isListItemDelimiter() -> {
      "$indent${text.trimStart()}"
    }

    isBlockQuoteTokenOrBlockQuoteTokenInWhiteSpace() -> {
      val beforeBracket = text.removeRegex("> ?")
      when {
        isBlockQuoteTokenInWhiteSpace() && !nextSibling().isEOL() -> beforeBracket

        beforeAnyTags || paragraphCount == 0 -> {
          beforeBracket + "> ".repeat(countParentBlockQuotes())
        }

        else -> "> ".repeat(countParentBlockQuotes())
          .prefix(if (addKDocLeadingSpace) "   " else "  ")
      }
    }

    else -> {
      when {
        isEOL() -> text
        nextLeaf()?.isListItemDelimiter() == true -> ""
        isWhiteSpace() -> {

          val count = nextLeaf()?.text.orEmpty().takeWhile { it.isWhitespace() }.length

          text.dropLast(count)
        }

        else -> {

          text
        }
      }
    }
  }
}

/** @since 1.0.4 */
internal fun MarkdownNode.indent(addKDocLeadingSpace: Boolean, beforeAnyTags: Boolean): String {

  val listLevel by lazy(NONE) { countParentLists() }
  val quoteLevel by lazy(NONE) { countParentBlockQuotes() }

  val leadingWhitespaces by lazy(NONE) {
    text.takeWhile { it.isWhitespace() }
      .ifEmpty {

        val prev = previousLeaf()
          ?.takeIf { !it.isEOL() }
          ?.text.orEmpty()

        prev.takeLastWhile { it.isWhitespace() }
      }
      .length
  }

  return when {
    isFirstParagraphInParent() -> ""

    isListItemDelimiter() && listLevel > 0 -> {

      parentListItems()
        .drop(1)
        .joinToString("") { listItemParent ->

          " ".repeat(listItemParent.listItemDelimiterUnsafe.textLengthWithoutWhitespace + 1)
        }
        .letIf(!beforeAnyTags) { prefix("  ") }
        .letIf(addKDocLeadingSpace) { prefix(" ") }
    }

    quoteLevel > 0 -> "> ".repeat(quoteLevel)
    !beforeAnyTags -> {

      "  ".letIf(addKDocLeadingSpace) { prefix(" ") }
        .drop(leadingWhitespaces)
    }

    else -> "".letIf(addKDocLeadingSpace) { prefix(" ") }.drop(leadingWhitespaces)
  }
}

/** @since 1.0.4 */
private fun MarkdownNode.continuationIndent(
  beforeAnyTags: Boolean,
  addKDocLeadingSpace: Boolean
): String {

  val leadingWhitespaces by lazy(NONE) { text.takeWhile { it.isWhitespace() }.length }

  val listLevel = countParentLists()
  val quoteLevel by lazy(NONE) { countParentBlockQuotes() }

  return when {
    listLevel > 0 -> {
      parentListItemsWithSelf()
        .joinToString("") { listItemParent ->
          " ".repeat(listItemParent.listItemDelimiterUnsafe.textLengthWithoutWhitespace + 1)
        }
        .letIf(!beforeAnyTags) { prefix("  ") }
        .drop(leadingWhitespaces)
    }

    quoteLevel > 0 -> "> ".repeat(quoteLevel)
      .letIf(!beforeAnyTags) { prefix("  ") }

    beforeAnyTags -> ""
    else -> "  "
  }.letIf(addKDocLeadingSpace) { prefix(" ") }
}
