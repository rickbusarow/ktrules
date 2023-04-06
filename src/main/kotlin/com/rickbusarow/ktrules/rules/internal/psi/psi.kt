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

package com.rickbusarow.ktrules.rules.internal.psi

import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isLeaf
import com.pinterest.ktlint.core.ast.prevLeaf
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.prefix
import com.rickbusarow.ktrules.rules.internal.trees.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import kotlin.LazyThreadSafetyMode.NONE

internal fun PsiElement.createFileFromText(text: String): KtFile {
  return PsiFileFactory.getInstance(project)
    .createFileFromText("tmp.kt", KotlinLanguage.INSTANCE, text) as KtFile
}

internal fun KDoc.getAllTags(): List<KDocTag> {
  return childrenDepthFirst()
    .filterIsInstance<KDocTag>()
    .filterNot { it.node.isKDocSection() && it.node.children().firstOrNull().isKDocTag() }
    .toList()
}

internal fun PsiElement?.isKDocTag(): Boolean = this != null && node.isKDocTag()
internal fun PsiElement?.isKDocSectionWithTagChildren(): Boolean =
  this != null && node.isKDocSectionWithTagChildren()

internal fun PsiElement?.isKDocTagWithTagChildren(): Boolean =
  this != null && node.isKDocTagWithTagChildren()

internal fun PsiElement?.isKDocDefaultSection(): Boolean {
  if (this !is KDocTag) return false

  val kdoc = getNonStrictParentOfType<KDoc>() ?: return false

  return this == kdoc.getDefaultSection()
}

internal fun PsiElement?.isKDocFirstSectionAfterDefault(): Boolean {
  return this?.node.isKDocFirstSectionAfterDefault()
}

internal fun PsiElement?.isInKDocDefaultSection(): Boolean {
  if (this == null) return false

  val tag = this as? KDocTag
    ?: getNonStrictParentOfType<KDocTag>()
    ?: return false

  return tag.parent is KDoc
}

internal fun <T : KDocTag> T.sectionTextWithoutLeadingAsterisks(): String {

  val shouldTrim = node.nextSibling().isKDocSection()

  val previousWhiteSpace by lazy(NONE) {

    node.prevLeaf(true)
      ?.takeIf { it.isKDocWhitespaceAfterLeadingAsterisk() }
      ?.text
      .orEmpty()
  }

  return node.childrenDepthFirst()
    .filter { it.psi != this@sectionTextWithoutLeadingAsterisks }
    .filter { it.isKDocTag() || it.isLeaf() }
    .takeWhile { !it.isKDocTag() }
    .toList()
    .dropLastWhile { shouldTrim && it.isKDocWhitespaceAfterLeadingAsterisk() }
    .joinToString("") { it.text }
    .replaceIndentByMargin("", "*")
    .letIf(shouldTrim) { removeSuffix("\n") }
    .prefix(previousWhiteSpace)
}

internal fun KDocTag.getKDocSection(): KDocSection =
  this as? KDocSection ?: getStrictParentOfType()!!

internal fun KDocTag.isBlank(): Boolean {
  return node.children()
    .filter { !it.isKDocLeadingAsterisk() }
    .singleOrNull()
    .isBlank()
}

internal val PsiElement.startOffset: Int get() = textRange.startOffset

internal fun KDoc.findIndent(): String {
  val fileLines = containingFile.text.lineSequence()

  var acc = startOffset + 1

  val numSpaces = fileLines.mapNotNull {
    if (it.length + 1 < acc) {
      acc -= (it.length + 1)
      null
    } else {
      acc
    }
  }
    .first()
  return " ".repeat(numSpaces)
}

internal fun PsiElement.childrenDepthFirst(): Sequence<PsiElement> {
  return depthFirstTraversal { children.toList() }
}

internal inline fun PsiElement.childrenDepthFirst(
  crossinline predicate: (PsiElement) -> Boolean
): Sequence<PsiElement> = depthFirstTraversal { children.filter(predicate) }

internal fun PsiElement.childrenBreadthFirst(): Sequence<PsiElement> {
  return breadthFirstTraversal { children.toList() }
}

internal inline fun PsiElement.childrenBreadthFirst(
  crossinline predicate: (PsiElement) -> Boolean
): Sequence<PsiElement> = breadthFirstTraversal { children.filter(predicate) }

internal inline fun <reified T : PsiElement> PsiElement.isPartOf(): Boolean =
  getNonStrictParentOfType<T>() != null
