/*
 * Copyright (C) 2025 Rick Busarow
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
import com.rickbusarow.ktrules.rules.internal.WrappingStyle.Companion.WRAPPING_STYLE_PROPERTY
import com.rickbusarow.ktrules.rules.internal.psi.children
import com.rickbusarow.ktrules.rules.internal.psi.fileIndent
import com.rickbusarow.ktrules.rules.internal.psi.getKDocSections
import com.rickbusarow.ktrules.rules.internal.psi.getKDocTextWithoutLeadingAsterisks
import com.rickbusarow.ktrules.rules.internal.psi.isKDoc
import com.rickbusarow.ktrules.rules.internal.psi.isKDocSection
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpace
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpaceWithNewline
import com.rickbusarow.ktrules.rules.internal.psi.prevLeaf
import com.rickbusarow.ktrules.rules.internal.psi.removeAllChildren
import com.rickbusarow.ktrules.rules.internal.psi.upsertWhitespaceBeforeMe
import com.rickbusarow.ktrules.rules.internal.removeRegex
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * Collapse short KDoc comments into a single line, like `/** my comment */`
 *
 * @since 1.0.7
 */
class KDocCollapseRule : RuleCompat(
  ID,
  visitorModifiers = setOf(
    mustRunAfter(KDocContentWrappingRule.ID),
    mustRunAfter(KDocLeadingAsteriskRule.ID),
    mustRunAfter(KDocBlankLinesRule.ID)
  ),
  usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY, WRAPPING_STYLE_PROPERTY)
) {

  private var maxLineLength: Int = MAX_LINE_LENGTH_PROPERTY.defaultValue

  private val skipAll by lazy { maxLineLength < 0 }

  override fun beforeFirstNode(editorConfig: EditorConfigCompat) {

    maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]

    super.beforeFirstNode(editorConfig)
  }

  override fun beforeVisitChildNodes(node: ASTNode, emit: EmitWithDecision) {

    if (skipAll) return
    if (!node.isKDoc()) return

    if (node.text.lines().size == 1) return

    val singleSection = node.getKDocSections()
      .filter { it.text.removeRegex("[* ]+").isNotBlank() }
      .singleOrNull()
      ?.takeIf { it.getKDocTextWithoutLeadingAsterisks().lines().size == 1 }
      ?: return

    val kdocStart = node.children().first()
    val kdocEnd = node.children().last()

    val indent = node.fileIndent(0)

    val sectionText = singleSection.getKDocTextWithoutLeadingAsterisks()

    val totalLength = sequenceOf(
      indent,
      kdocStart.text,
      sectionText.trimPreservingCodeBlockIndent(),
      kdocEnd.text
    )
      .sumOf { it.length }
      // Add two spaces to account for one space after the KDOC_START and one before KDOC_END
      .plus(2)

    if (totalLength > maxLineLength) return

    emit(node.startOffset, ERROR_MESSAGE, true)
      .ifAutocorrectAllowed {

        // If the second child of the KDoc is a whitespace, that means there's a newline and that
        //    newline should be removed.
        //
        // If there is no newline, the whitespace is included in the default KDOC_SECTION.
        //
        // If the first actual content of the KDoc is a tag, there will still be a default section and
        //    that default section will only contain a single WHITE_SPACE child.
        node.children()
          .drop(1)
          .firstOrNull()
          .takeIf { it.isWhiteSpace() }
          ?.let {
            node.removeChild(it)
          }

        val defaultSection = node.children().first { it.isKDocSection() }

        val defaultSectionText = defaultSection.getKDocTextWithoutLeadingAsterisks()
          .trimPreservingCodeBlockIndent()

        defaultSection.removeAllChildren()

        val newDefaultSectionText = when {
          defaultSectionText.startsWith(' ') -> "$defaultSectionText "
          defaultSectionText.isNotBlank() -> " $defaultSectionText "
          else -> " "
        }

        defaultSection.addChild(LeafPsiElement(ElementType.KDOC_TEXT, newDefaultSectionText), null)

        val whiteSpaceBeforeEnd = kdocEnd.prevLeaf(includeEmpty = true)
          .takeIf { it.isWhiteSpace() }

        when {
          // If there is whitespace before the end and only one KDoc section, remove the whitespace
          whiteSpaceBeforeEnd != null && node.children().count { it.isKDocSection() } == 1 -> {
            node.removeChild(whiteSpaceBeforeEnd)
          }

          // If there is no whitespace before the end and more than one KDoc section, add a space
          whiteSpaceBeforeEnd == null && node.children().count { it.isKDocSection() } > 1 -> {
            kdocEnd.upsertWhitespaceBeforeMe(" ")
          }

          // If there is whitespace before the end and it's a newline, replace it with a space
          whiteSpaceBeforeEnd != null && whiteSpaceBeforeEnd.isWhiteSpaceWithNewline() -> {
            node.replaceChild(whiteSpaceBeforeEnd, PsiWhiteSpaceImpl(" "))
          }
        }
      }
  }

  private fun String.trimPreservingCodeBlockIndent() = removeRegex("^ {1,3}(?=[^ ])", "\\s$")

  internal companion object {

    val ID = RuleId("kdoc-collapse")
    const val ERROR_MESSAGE = "kdoc should be collapsed into a single line"
  }
}
