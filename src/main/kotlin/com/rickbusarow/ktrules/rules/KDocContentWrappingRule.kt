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

import com.rickbusarow.ktrules.compat.EditorConfigCompat
import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.MAX_LINE_LENGTH_PROPERTY
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.compat.mustRunAfter
import com.rickbusarow.ktrules.rules.WrappingStyle.GREEDY
import com.rickbusarow.ktrules.rules.WrappingStyle.MINIMUM_RAGGED
import com.rickbusarow.ktrules.rules.internal.markdown.MarkdownNode
import com.rickbusarow.ktrules.rules.internal.markdown.wrap
import com.rickbusarow.ktrules.rules.internal.prefixIfNot
import com.rickbusarow.ktrules.rules.internal.psi.children
import com.rickbusarow.ktrules.rules.internal.psi.childrenBreadthFirst
import com.rickbusarow.ktrules.rules.internal.psi.createFileFromText
import com.rickbusarow.ktrules.rules.internal.psi.fileIndent
import com.rickbusarow.ktrules.rules.internal.psi.getAllTags
import com.rickbusarow.ktrules.rules.internal.psi.isBlank
import com.rickbusarow.ktrules.rules.internal.psi.isInKDocDefaultSection
import com.rickbusarow.ktrules.rules.internal.psi.isKDocLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.nextLeaf
import com.rickbusarow.ktrules.rules.internal.psi.nextSibling
import com.rickbusarow.ktrules.rules.internal.psi.parent
import com.rickbusarow.ktrules.rules.internal.psi.removeAllChildren
import com.rickbusarow.ktrules.rules.internal.psi.removeLastChildrenWhile
import com.rickbusarow.ktrules.rules.internal.psi.startOffset
import com.rickbusarow.ktrules.rules.internal.psi.tagTextWithoutLeadingAsterisks
import com.rickbusarow.ktrules.rules.internal.psi.upsertWhitespaceAfterMe
import com.rickbusarow.ktrules.rules.wrapping.GreedyWrapper
import com.rickbusarow.ktrules.rules.wrapping.MinimumRaggednessWrapper
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Fixes wrapping inside KDoc comments.
 *
 * @since 1.0.0
 */
class KDocContentWrappingRule : RuleCompat(
  ID,
  visitorModifiers = setOf(
    mustRunAfter(KDocLeadingAsteriskRule.ID),
    mustRunAfter(KDocIndentAfterLeadingAsteriskRule.ID)
  ),
  usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY, WRAPPING_STYLE_PROPERTY)
) {

  private var maxLineLength: Int = MAX_LINE_LENGTH_PROPERTY.defaultValue

  private val markdownParser by lazy(NONE) {
    // This has to use the GFMFlavourDescriptor so that it can parse tables.
    // The CommonMarkFlavourDescriptor does not recognize tables.
    MarkdownParser(GFMFlavourDescriptor())
  }

  private var wrappingStyle: WrappingStyle = WRAPPING_STYLE_PROPERTY.defaultValue

  private val wrapper by lazy(NONE) {
    when (wrappingStyle) {
      GREEDY -> GreedyWrapper()
      MINIMUM_RAGGED -> MinimumRaggednessWrapper()
    }
  }

  private val skipAll by lazy { maxLineLength < 0 }

  override fun beforeFirstNode(editorConfig: EditorConfigCompat) {

    maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]
    wrappingStyle = editorConfig[WRAPPING_STYLE_PROPERTY]
    super.beforeFirstNode(editorConfig)
  }

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (!skipAll && node.elementType == ElementType.KDOC) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi as KDoc

    val starIndent = kdoc.fileIndent(additionalOffset = 1)

    val tags = kdoc.getAllTags()

    val notWrapped = tags.getFixedTagPairs(starIndent)

    notWrapped.forEach { (tag, wrapped) ->

      emit(tag.startOffset, ERROR_MESSAGE, true)

      if (autoCorrect) {

        tag.fix(wrapped = wrapped, kdoc = kdoc, starIndent = starIndent)
      }
    }
  }

  private fun KDocTag.fix(
    wrapped: String,
    kdoc: KDoc,
    starIndent: String
  ) {

    val inKDocDefaultSection = node.isInKDocDefaultSection()
    val wrappedLines = wrapped.lines()

    val singleLineKDoc = wrappedLines.size == 1 && kdoc.text.lines().size == 1

    val wrappedAsKDoc = getWrappedAsKDocString(
      singleLineKDoc = singleLineKDoc,
      wrapped = wrapped,
      wrappedLines = wrappedLines,
      inKDocDefaultSection = inKDocDefaultSection,
      starIndent = starIndent
    )

    val toDelete = node.children()
      .takeWhile { !it.isKDocTag() }
      .toList()
      .dropLastWhile { it.isKDocLeadingAsterisk() || it.isBlank() }

    val wrappedSection = getWrappedSectionPsi(
      wrappedAsKdoc = wrappedAsKDoc,
    )

    if (toDelete.isEmpty()) {
      node.parent!!.replaceChild(node, wrappedSection.node)
    } else {
      val anchor = toDelete.first()

      val makingMultiLine = wrappedLines.size > 1 && kdoc.text.lines().size == 1

      if (makingMultiLine) {

        val open = kdoc.node.firstChildNode
        val afterOpen = open.nextSibling()
        kdoc.node.addChild(PsiWhiteSpaceImpl("\n$starIndent"), afterOpen)

        if (!wrappedSection.node.nextLeaf().isKDocLeadingAsterisk()) {
          kdoc.node.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), afterOpen)
        }
      }

      wrappedSection.node
        .children()
        .forEach { new ->
          node.addChild(new.clone() as ASTNode, anchor)
        }

      if (makingMultiLine) {
        node.upsertWhitespaceAfterMe("\n$starIndent")
      }

      toDelete.forEach { node.removeChild(it) }
    }
  }

  private fun getWrappedAsKDocString(
    singleLineKDoc: Boolean,
    wrapped: String,
    wrappedLines: List<String>,
    inKDocDefaultSection: Boolean,
    starIndent: String,
  ): String = if (singleLineKDoc) {
    "/**${wrapped.prefixIfNot(" ")} */"
  } else {
    buildString {
      appendLine("/**")

      wrappedLines.forEachIndexed { i, line ->

        val trimmedLine = line.trimEnd()

        when {
          i == 0 && inKDocDefaultSection -> appendLine("$starIndent*$trimmedLine")
          line.isEmpty() -> appendLine("$starIndent*")
          !line.startsWith(" ") -> appendLine("$starIndent* $trimmedLine")
          else -> appendLine("$starIndent*$trimmedLine")
        }
      }

      appendLine("$starIndent*/")
    }
  }

  private fun KDocTag.getWrappedSectionPsi(
    wrappedAsKdoc: String,
  ): KDocTag {

    return createFileFromText(wrappedAsKdoc)
      .childrenBreadthFirst()
      .filterIsInstance<KDocTag>()
      .toList()
      .last()
      .removeAllChildren { it.isKDocTag() }
      .also { tag ->
        tag.node.removeLastChildrenWhile { it.isKDocLeadingAsterisk() || it.isBlank() }
      }
  }

  private fun List<KDocTag>.getFixedTagPairs(starIndent: String): List<Pair<KDocTag, String>> {
    return mapIndexedNotNull { tagIndex, tag ->

      val beforeAnyTags = tag.node.isInKDocDefaultSection() && tagIndex == 0

      val sectionText = tag.tagTextWithoutLeadingAsterisks()

      val maxLength = maxLineLength - (starIndent.length + 1)

      val wrapped = MarkdownNode.from(
        markdown = sectionText,
        markdownParser = markdownParser
      )
        .wrap(
          wrapper = wrapper,
          maxLength = maxLength,
          beforeAnyTags = beforeAnyTags,
          addKDocLeadingSpace = true
        )

      if (wrapped.trim() != sectionText.trim()) {
        tag to wrapped
      } else {
        null
      }
    }
  }

  internal companion object {

    val ID = RuleId("kdoc-content-wrapping")
    const val ERROR_MESSAGE = "kdoc content wrapping"
  }
}
