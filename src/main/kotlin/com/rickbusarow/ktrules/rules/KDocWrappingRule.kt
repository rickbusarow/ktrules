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
import com.pinterest.ktlint.core.Rule.VisitorModifier.RunAfterRule
import com.pinterest.ktlint.core.api.EditorConfigProperties
import com.pinterest.ktlint.core.api.UsesEditorConfigProperties
import com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty
import com.pinterest.ktlint.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.lastChildLeafOrSelf
import com.rickbusarow.ktrules.rules.WrappingStyle.GREEDY
import com.rickbusarow.ktrules.rules.WrappingStyle.MINIMUM_RAGGED
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.markdown.MarkdownNode
import com.rickbusarow.ktrules.rules.internal.markdown.wrap
import com.rickbusarow.ktrules.rules.internal.prefixIfNot
import com.rickbusarow.ktrules.rules.internal.psi.childrenBreadthFirst
import com.rickbusarow.ktrules.rules.internal.psi.createFileFromText
import com.rickbusarow.ktrules.rules.internal.psi.findIndent
import com.rickbusarow.ktrules.rules.internal.psi.getAllTags
import com.rickbusarow.ktrules.rules.internal.psi.getKDocSection
import com.rickbusarow.ktrules.rules.internal.psi.isInKDocDefaultSection
import com.rickbusarow.ktrules.rules.internal.psi.isKDocLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTagOrSection
import com.rickbusarow.ktrules.rules.internal.psi.isKDocWhitespaceAfterLeadingAsterisk
import com.rickbusarow.ktrules.rules.internal.psi.parent
import com.rickbusarow.ktrules.rules.internal.psi.sectionTextWithoutLeadingAsterisks
import com.rickbusarow.ktrules.rules.internal.psi.startOffset
import com.rickbusarow.ktrules.rules.wrapping.GreedyWrapper
import com.rickbusarow.ktrules.rules.wrapping.MinimumRaggednessWrapper
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Fixes wrapping inside KDoc comments.
 *
 * @since 1.0.0
 */
class KDocWrappingRule : Rule(
  id = "kdoc-wrapping",
  visitorModifiers = setOf(
    RunAfterRule("kdoc-leading-asterisk"),
    RunAfterRule("kdoc-indent-after-leading-asterisk")
  )
), UsesEditorConfigProperties {

  private val maxLineLengthProperty = MAX_LINE_LENGTH_PROPERTY
  private var maxLineLength: Int = maxLineLengthProperty.defaultValue

  private val markdownParser by lazy(NONE) {
    // This has to use the GFMFlavourDescriptor so that it can parse tables.
    // The CommonMarkFlavourDescriptor does not recognize tables.
    MarkdownParser(GFMFlavourDescriptor())
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

    val starIndent = kdoc.findIndent()

    val tags = kdoc.getAllTags()

    val notWrapped = tags.getFixedTagPairs(starIndent)

    notWrapped.forEach { (tag, wrapped) ->

      emit(tag.startOffset, "kdoc line wrapping", true)

      if (autoCorrect) {
        tag.fix(wrapped = wrapped, kdoc = kdoc, starIndent = starIndent, tags = tags)
      }
    }
  }

  private fun KDocTag.fix(
    wrapped: String,
    kdoc: KDoc,
    starIndent: String,
    tags: List<KDocTag>
  ) {

    val inKDocDefaultSection = node.isInKDocDefaultSection()
    val wrappedLines = wrapped.lines()

    val singleLineKDoc = wrappedLines.size == 1 && kdoc.text.lines().size == 1

    val wrappedAsKDoc = getWrappedAsKDoc(
      singleLineKDoc = singleLineKDoc,
      wrapped = wrapped,
      wrappedLines = wrappedLines,
      inKDocDefaultSection = inKDocDefaultSection,
      starIndent = starIndent,
      isLastTag = tags.lastOrNull() == this@fix
    )

    val tagIndex = tags.indexOf(this)

    val nextTagOrNull = tags.getOrNull(tagIndex + 1)
    val nextTagIsInSameSection = nextTagOrNull?.getKDocSection() == getKDocSection()

    val wrappedSection = getWrappedSectionPsi(
      wrappedAsKdoc = wrappedAsKDoc,
      nextTagIsInSameSection = nextTagIsInSameSection,
      tagIndex = tagIndex,
      nextTagOrNull = nextTagOrNull
    )

    val toDelete = node.children()
      .takeWhile { !it.isKDocTag() }
      .toList()
      .letIf(nextTagOrNull != null) {

        var count = 0

        @Suppress("MagicNumber")
        val keep = if (nextTagIsInSameSection) 2 else 4

        dropLastWhile { node ->

          if (++count < keep) return@dropLastWhile false

          node.isKDocWhitespaceAfterLeadingAsterisk() || node.isKDocLeadingAsterisk()
        }
      }

    if (toDelete.isEmpty()) {
      node.parent!!.replaceChild(node, wrappedSection.node)
    } else {
      val anchor = toDelete.first()

      wrappedSection.node.children()
        .toList()
        .forEach { new ->
          node.addChild(new, anchor)
        }

      toDelete.forEach { node.removeChild(it) }
    }
  }

  private fun getWrappedAsKDoc(
    singleLineKDoc: Boolean,
    wrapped: String,
    wrappedLines: List<String>,
    inKDocDefaultSection: Boolean,
    starIndent: String,
    isLastTag: Boolean,
  ) = if (singleLineKDoc) {
    "/**${wrapped.prefixIfNot(" ")} */"
  } else {
    buildString {
      appendLine("/**")

      wrappedLines.forEachIndexed { i, line ->

        when {
          i == 0 && inKDocDefaultSection -> appendLine("$starIndent*$line")
          line.isBlank() -> appendLine("$starIndent*")
          !line.startsWith(" ") -> appendLine("$starIndent* $line")
          else -> appendLine("$starIndent*$line")
        }
      }

      if (inKDocDefaultSection && !isLastTag) {
        appendLine("$starIndent*")
      }

      appendLine("$starIndent*/")
    }
  }

  private fun KDocTag.getWrappedSectionPsi(
    wrappedAsKdoc: String,
    nextTagIsInSameSection: Boolean,
    tagIndex: Int,
    nextTagOrNull: KDocTag?
  ): KDocTag = createFileFromText(wrappedAsKdoc)
    .childrenBreadthFirst()
    .last { psi ->
      psi.node.isKDocTagOrSection() && psi.children
        .none { child -> child.node.isKDocTag() }
    }
    .let { it as KDocTag }
    .also { newTag ->

      val newTagNode = newTag.node

      if (nextTagIsInSameSection) {

        repeat(2) {
          val toDelete = newTagNode.children()
            .lastOrNull()
            .takeIf { newNode ->
              newNode.isKDocWhitespaceAfterLeadingAsterisk() ||
                newNode.isKDocLeadingAsterisk() ||
                newNode.isWhiteSpaceWithNewline()
            }
            ?: return@repeat

          newTagNode.removeChild(toDelete)
        }

        if (tagIndex == 0) {
          newTagNode.addChild(PsiWhiteSpaceImpl(" "), null)
        }
      } else if (nextTagOrNull != null && newTagNode.lastChildLeafOrSelf()
          .isKDocLeadingAsterisk()
      ) {

        newTagNode.addChild(PsiWhiteSpaceImpl(" "), null)
      }
    }

  private fun List<KDocTag>.getFixedTagPairs(starIndent: String): List<Pair<KDocTag, String>> {
    return mapIndexedNotNull { tagIndex, tag ->

      val beforeAnyTags = tag.node.isInKDocDefaultSection() && tagIndex == 0

      val sectionText = tag.sectionTextWithoutLeadingAsterisks()

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

      (tag to wrapped).takeIf { it.second != sectionText }
    }
  }
}
