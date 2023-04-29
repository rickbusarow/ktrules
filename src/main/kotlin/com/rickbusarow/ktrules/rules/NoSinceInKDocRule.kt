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
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.mapLines
import com.rickbusarow.ktrules.rules.internal.prefixIfNot
import com.rickbusarow.ktrules.rules.internal.psi.children
import com.rickbusarow.ktrules.rules.internal.psi.createKDoc
import com.rickbusarow.ktrules.rules.internal.psi.fileIndent
import com.rickbusarow.ktrules.rules.internal.psi.getAllTags
import com.rickbusarow.ktrules.rules.internal.psi.getChildOfType
import com.rickbusarow.ktrules.rules.internal.psi.isInKDocDefaultSection
import com.rickbusarow.ktrules.rules.internal.psi.isInKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTag
import com.rickbusarow.ktrules.rules.internal.psi.isKDocTagName
import com.rickbusarow.ktrules.rules.internal.psi.ktPsiFactory
import com.rickbusarow.ktrules.rules.internal.removeRegex
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag.SINCE
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * Finds Kdoc comments which don't have a `@since <version>` annotation.
 *
 * @since 1.0.1
 */
class NoSinceInKDocRule : RuleCompat(
  ruleId = ID,
  usesEditorConfigProperties = setOf(PROJECT_VERSION_PROPERTY)
) {

  private var currentVersion: String? = null

  private val skipAll by lazy {
    currentVersion?.matches(".*?-.*$".toRegex()) == true
  }

  override fun beforeFirstNode(editorConfig: EditorConfigCompat) {

    val version = editorConfig[PROJECT_VERSION_PROPERTY]
      ?: System.getProperty("ktrules.project_version")

    if (version != null) {
      currentVersion = version
    }
  }

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (currentVersion != null && !skipAll && node.elementType == ElementType.KDOC_END) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi.parent as KDoc

    val tag = kdoc.findSinceTag()

    if (tag == null) {

      emit(kdocNode.startOffset, "add `@since $currentVersion` to kdoc", true)

      if (autoCorrect) {
        kdocNode.addSinceTag(currentVersion!!)
      }
      return
    }

    val sinceVersion = kdoc.findSinceTag()?.getContent()

    if (sinceVersion.isNullOrBlank()) {

      emit(
        kdocNode.startOffset,
        "add '$currentVersion' to `@since` tag",
        true
      )

      if (autoCorrect) {
        tag.addVersionToSinceTag(currentVersion!!)
      }
    }
  }

  private fun KDoc.findSinceTag(): KDocTag? {
    // Pre-existing 'since' tags which are parsed before visiting will show up as KDocTag. They're
    // nested inside KDocSections, so they don't show up using AST's non-recursive traversals.
    // After we've added our own tag, it won't show up in PSI -- but it's flat inside the KDoc node,
    // so it shows up with simple AST traversal.  Note that the PSI version has a name of 'since'
    // but the AST version node is '@since'.  This is consistent whether the tag is added manually
    // here, or if it's parsed that way from source.
    return getAllTags()
      .singleOrNull { it.name == "since" }
      ?: node.children()
        .filter { it.isKDocTag() }
        .filter { kdocTag ->
          kdocTag.children()
            .filter { it.isKDocTagName() }
            .any { it.text == "@since" }
        }
        .singleOrNull()
        ?.let { KDocTag(it) }
  }

  private fun ASTNode.addSinceTag(version: String) {

    val kdoc = psi.parent as KDoc

    val sections = kdoc.getAllSections()
      .map { section ->
        section.text
          .mapLines { it.removeRegex("^\\s*\\*") }
          .letIf(section.isInKDocDefaultSection() && !section.node.isInKDocTag()) {
            removeSuffix(" ")
              .removeSuffix("\n")
          }
          .prefixIfNot(" ")
      }
      .filterNot { it.isBlank() }

    val defaultSection = kdoc.getDefaultSection()
    val defaultSectionText = defaultSection.text

    val leadingNewlineOrBlank = if (
      defaultSectionText.isNotBlank() &&
      defaultSection.getChildOfType<KDocTag>() == null &&
      sections.singleOrNull()?.endsWith("\n\n") == false
    ) {
      "\n"
    } else {
      ""
    }

    val newKdoc = kdoc.ktPsiFactory()
      .createKDoc(sections + "$leadingNewlineOrBlank @since $version", kdoc.fileIndent(0))

    kdoc.replace(newKdoc)
  }

  private fun KDocTag.addVersionToSinceTag(version: String) {

    require(knownTag == SINCE) {
      "Expected to be adding a version to a `@since` tag, but instead it's `$text`."
    }

    node.addChild(LeafPsiElement(ElementType.KDOC_TEXT, " $version"), null)
  }

  internal companion object {

    val ID = RuleId("no-since-in-kdoc")
  }
}
