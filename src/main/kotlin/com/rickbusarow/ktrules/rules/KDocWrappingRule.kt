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

package com.rickbusarow.ktrules.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.api.EditorConfigProperties
import com.pinterest.ktlint.core.api.UsesEditorConfigProperties
import com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty
import com.pinterest.ktlint.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.children
import com.rickbusarow.ktrules.rules.StringWrapper.Companion.splitWords
import com.rickbusarow.ktrules.rules.WrappingStyle.GREEDY
import com.rickbusarow.ktrules.rules.WrappingStyle.MINIMUM_RAGGED
import com.rickbusarow.ktrules.rules.internal.findIndent
import com.rickbusarow.ktrules.rules.internal.getAllTags
import com.rickbusarow.ktrules.rules.internal.isKDocLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.isKDocTag
import com.rickbusarow.ktrules.rules.internal.isKDocText
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.mapLines
import com.rickbusarow.ktrules.rules.internal.remove
import com.rickbusarow.ktrules.rules.internal.startOffset
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownTokenTypes.Companion.EOL
import org.intellij.markdown.MarkdownTokenTypes.Companion.WHITE_SPACE
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Fixes wrapping inside KDoc comments.
 *
 * [Markdown link](https://example.com)
 *
 * @since 1.0.0
 */
class KDocWrappingRule : Rule(id = "kdoc-wrapping"), UsesEditorConfigProperties {

  private val maxLineLengthProperty = MAX_LINE_LENGTH_PROPERTY
  private var maxLineLength: Int = maxLineLengthProperty.defaultValue

  private val markdownParser by lazy(NONE) {
    MarkdownParser(CommonMarkFlavourDescriptor())
  }

  override val editorConfigProperties: List<EditorConfigProperty<*>>
    get() = listOf(maxLineLengthProperty, WRAPPING_STYLE_PROPERTY)

  private var wrappingStyle: WrappingStyle = WRAPPING_STYLE_PROPERTY.defaultValue

  private val wrapper by lazy(NONE) {
    when (wrappingStyle) {
      GREEDY -> GreedyWrapper()
      MINIMUM_RAGGED -> MinimumRaggednessWrapper()
    }
  }

  private val skipAll by lazy { maxLineLength < 0 }

  override fun beforeFirstNode(editorConfigProperties: EditorConfigProperties) {

    maxLineLength = editorConfigProperties.getEditorConfigValue(maxLineLengthProperty)
    wrappingStyle = editorConfigProperties.getEditorConfigValue(WRAPPING_STYLE_PROPERTY)
    super.beforeFirstNode(editorConfigProperties)
  }

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (!skipAll && node.elementType == ElementType.KDOC_START) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi.parent as KDoc

    val starIndent = kdoc.findIndent()

    // KDocSection is a subtype of KDocTag.  The default section is a tag.
    val tags = kdoc.getAllTags()

    tags.forEachIndexed { tagIndex, tag ->

      val lastTag = tagIndex == tags.lastIndex

      val textNodesEtc = tag.node.children()
        .dropWhile { !it.isKDocText() }
        .takeWhile { !it.isKDocTag() }
        .toList()

      val sectionText = textNodesEtc
        .joinToString("") { it.text }
        .mapLines { it.remove("""^ *\*? ?""".toRegex()) }
        .trimEnd()

      val wrapped = tag.sectionContent(starIndent.length + 2)

      if (sectionText != wrapped) {

        emit(tag.startOffset, "kdoc line wrapping", true)

        if (autoCorrect) {

          val tagNode = tag.node as CompositeElement

          val anchor = textNodesEtc.firstOrNull()

          val newlineIndent = "\n$starIndent"

          val wrappedLines = wrapped.lines()

          var insideFencedCodeBlock = false

          wrappedLines
            .forEachIndexed { i, line ->

              if (line.startsWith("```")) {
                insideFencedCodeBlock = !insideFencedCodeBlock
              }

              if (i != 0) {
                tagNode.addChild(PsiWhiteSpaceImpl(newlineIndent), anchor)
                tagNode.addChild(LeafPsiElement(KDOC_LEADING_ASTERISK, "*"), anchor)

                // Make sure that every non-blank line has at least one white space between the leading
                // asterisk and the first non-whitespace character.  BUT, if the line has at least
                // three leading white spaces, leave it alone.  At 3 spaces, IntelliJ and Dokka start
                // treating it as a code block.
                if (line.isNotBlank() && (!line.startsWith("   ") || insideFencedCodeBlock)) {
                  tagNode.addChild(PsiWhiteSpaceImpl(" "), anchor)
                }
              }

              if (i == 0 && tag.parent == kdoc) {

                tagNode.addChild(LeafPsiElement(KDOC_TEXT, " $line"), anchor)
              } else {
                tagNode.addChild(LeafPsiElement(KDOC_TEXT, line), anchor)
              }
            }

          if (!lastTag) {
            if (tag.parent == kdoc) {
              tagNode.addChild(PsiWhiteSpaceImpl(newlineIndent), anchor)
              tagNode.addChild(LeafPsiElement(KDOC_LEADING_ASTERISK, "*"), anchor)
              tagNode.addChild(PsiWhiteSpaceImpl(newlineIndent), anchor)
              tagNode.addChild(LeafPsiElement(KDOC_LEADING_ASTERISK, "*"), anchor)
              tagNode.addChild(PsiWhiteSpaceImpl(" "), anchor)
            }
          }

          textNodesEtc.toList().forEach { tagNode.removeChild(it) }
        }
      }
    }
  }

  private fun KDocTag.sectionContent(indentLength: Int): String {

    val sectionText = node.children()
      // strip away the LEADING asterisk (from the first line), as well as any `@___` tags and any
      // links like `myParameter` or `IllegalStateException`
      .dropWhile { !it.isKDocText() }
      .takeWhile { !it.isKDocTag() }
      .joinToString("") { it.text }
      .mapLines { it.remove("""^ *\*?""".toRegex()) }

    val skip = setOf(WHITE_SPACE, EOL)

    val maxLength = maxLineLength - indentLength

    return markdownParser.buildMarkdownTreeFromString(sectionText)
      .children
      .asSequence()
      .filterNot { it.type in skip }
      .mapIndexed { paragraphNumber: Int, markdownNode ->

        val leadingIndent = if (parent.node.elementType != KDOC) {
          if (paragraphNumber == 0) {
            node.children()
              .dropWhile { it.isKDocLeadingAsterisk() }
              .takeWhile { !it.isKDocText() }
              .joinToString("") { " ".repeat(it.text.length) }
          } else {
            "  "
          }
        } else {
          ""
        }

        val continuationIndentLength = if (parent.node.elementType == KDOC) 0 else 2
        val continuationIndent = " ".repeat(continuationIndentLength)

        when (markdownNode.type) {
          PARAGRAPH -> wrapper.wrap(
            words = markdownNode.getTextInNode(sectionText).cleanWhitespaces().splitWords(),
            maxLength = maxLength - continuationIndentLength,
            leadingIndent = leadingIndent,
            continuationIndent = continuationIndent
          )
            .letIf(paragraphNumber == 0 || parent.node.elementType == KDOC) {
              trimStart()
            }

          CODE_BLOCK -> if (parent.node.elementType == KDOC) {
            // If a CODE_BLOCK is top-level within the default section, that means it's an indented
            // block without the code fence (```).  That means we should leave it alone, but there's a
            // discrepancy in how the whitespace immediately after the leading asterisk is handled by
            // `sectionText` versus how it's reported by the Markdown library's `getTextInNode`.  Even
            // though getTextInNode is just pulling it from sectionText, it's giving an extra
            // whitespace to the start of each line.  We remove it here to avoid a false positive when
            // comparing the wrapped blob to the original.
            markdownNode.getTextInNode(sectionText).toString()
              .mapLines { it.drop(1).trimEnd() }
          } else {
            // If a CODE_BLOCK is top-level within a section/tag other than the default, that means
            // it's not wrapped in three backticks.  Assume this means it's a multi-line description of
            // a tag, and it's just indented.
            wrapper.wrap(
              words = markdownNode.getTextInNode(sectionText).cleanWhitespaces().splitWords(),
              maxLength = maxLength - continuationIndentLength,
              leadingIndent = leadingIndent,
              continuationIndent = continuationIndent
            )
          }

          // code fences, headers, tables, etc. don't get wrapped
          else -> {
            markdownNode.getTextInNode(sectionText).toString().trimIndent()
          }
        }
      }
      .joinToString("\n\n")
  }

  private fun CharSequence.cleanWhitespaces(): String {
    return replace("(\\S)\\s+".toRegex(), "$1 ").trim()
  }
}
