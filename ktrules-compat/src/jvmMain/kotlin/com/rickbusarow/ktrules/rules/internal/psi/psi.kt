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

import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.prefix
import com.rickbusarow.ktrules.rules.internal.requireNotNull
import com.rickbusarow.ktrules.rules.internal.trees.Traversals.breadthFirstTraversal
import com.rickbusarow.ktrules.rules.internal.trees.Traversals.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @return a [KtPsiFactory] instance for the project of this [PsiElement].
 * @since 1.1.0
 */
fun PsiElement.ktPsiFactory(): KtPsiFactory {
  return KtPsiFactory(project, markGenerated = true)
}

/**
 * @param text the text of the new [KtFile].
 * @return a new [KtFile] instance created from the given text.
 * @since 1.1.0
 */
fun PsiElement.createFileFromText(text: String): KtFile = ktPsiFactory().createFileFromText(text)

/**
 * @param text the text of the new [KtFile].
 * @return a new [KtFile] instance created from the given text.
 * @since 1.1.1
 */
fun KtPsiFactory.createFileFromText(text: String): KtFile = createFile("tmp.kt", text)

/**
 * @return a list of all [KDocTag]s in this [KDoc].
 * @since 1.1.0
 */
fun KDoc.getAllTags(): List<KDocTag> {
  return childrenDepthFirst()
    .filterIsInstance<KDocTag>()
    .filterNot { it.node.isKDocSection() && it.node.children().firstOrNull().isKDocTag() }
    .toList()
}

/**
 * @return `true` if this [PsiElement] is a [KDocTag] and its [node][PsiElement.getNode] is a KDoc
 *   tag node, `false` otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isKDocTag(): Boolean {
  return this != null && node.isKDocTag()
}

/**
 * @return `true` if this [PsiElement] is a KDoc section with tag children, `false` otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isKDocSectionWithTagChildren(): Boolean {
  return this != null && node.isKDocSectionWithTagChildren()
}

/**
 * @return `true` if this [PsiElement] is a KDoc tag with tag children, `false` otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isKDocTagWithTagChildren(): Boolean {
  return this != null && node.isKDocTagWithTagChildren()
}

/**
 * @return `true` if this [PsiElement] is in the default section of a KDoc comment, `false`
 *   otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isKDocDefaultSection(): Boolean {
  if (this !is KDocTag) return false

  val kdoc = getNonStrictParentOfType<KDoc>() ?: return false

  return this == kdoc.getDefaultSection()
}

/**
 * @return `true` if this [PsiElement] is the first section after the default section in a KDoc
 *   comment, `false` otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isKDocFirstSectionAfterDefault(): Boolean {
  return this?.node.isKDocFirstSectionAfterDefault()
}

/**
 * @return `true` if this [PsiElement] is in the default section of a KDoc comment, `false`
 *   otherwise.
 * @since 1.1.0
 */
fun PsiElement?.isInKDocDefaultSection(): Boolean {
  if (this == null) return false

  val tag = this as? KDocTag
    ?: getNonStrictParentOfType<KDocTag>()
    ?: return false

  return tag.parent is KDoc
}

/**
 * Returns the text content of this [KDocTag] without any leading asterisks, optionally trimming the
 * trailing whitespace if this tag is in a KDoc section that has tags after it.
 *
 * @return the text content of this [KDocTag] without leading asterisks.
 * @since 1.1.0
 */
fun <T : KDocTag> T.tagTextWithoutLeadingAsterisks(): String {
  val shouldTrim = node.nextSibling().isKDocSection()

  val previousWhiteSpace by lazy(NONE) {

    node.prevLeaf(true)
      ?.takeIf { it.isKDocWhitespaceAfterLeadingAsterisk() }
      ?.text
      .orEmpty()
  }

  return node.childrenDepthFirst()
    .filter { it.psi != this@tagTextWithoutLeadingAsterisks }
    .filter { it.isKDocTag() || it.isLeaf() }
    .takeWhile { !it.isKDocTag() }
    .toList()
    .dropLastWhile { shouldTrim && it.isKDocWhitespaceAfterLeadingAsterisk() }
    .joinToString("") { it.text }
    .replaceIndentByMargin("", "*")
    .letIf(shouldTrim) { removeSuffix("\n") }
    .prefix(previousWhiteSpace)
}

/**
 * @return the [KDocSection] of this [KDocTag]. If the receiver tag is a [KDocSection], it will
 *   return itself.
 * @since 1.1.0
 * @throws IllegalArgumentException if this [KDocTag] doesn't have a [KDocSection] parent.
 */
fun KDocTag.getKDocSection(): KDocSection {
  return this as? KDocSection ?: getStrictParentOfType<KDocSection>()
    .requireNotNull {
      "The receiver KDocTag element ${this@getKDocSection} does not have a KDocSection parent."
    }
}

/**
 * @return `true` if this [KDocTag] is blank, `false` otherwise.
 * @since 1.1.0
 */
fun KDocTag.isBlank(): Boolean {
  return node.children()
    .filter { !it.isKDocLeadingAsterisk() }
    .singleOrNull()
    .isBlank()
}

/**
 * @return the start offset of this [PsiElement].
 * @since 1.1.0
 */
val PsiElement.startOffset: Int get() = textRange.startOffset

/**
 * Returns the indentation string of this [PsiElement]'s containing file, up to this element's start
 * offset plus the specified additional offset.
 *
 * @param additionalOffset the additional offset to add to this element's start offset when
 *   computing the indentation.
 * @return the indentation string of this element's containing file.
 * @since 1.1.0
 */
fun PsiElement.fileIndent(additionalOffset: Int): String {
  val fileLines = containingFile.text.lineSequence()

  var acc = startOffset + additionalOffset

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

/**
 * @return a depth-first [Sequence] of this [PsiElement]'s descendants.
 * @since 1.1.0
 */
fun PsiElement.childrenDepthFirst(): Sequence<PsiElement> {
  return depthFirstTraversal(this) { children.toList() }
}

/**
 * Returns a depth-first [Sequence] of all of this [PsiElement]'s descendants that satisfy the
 * specified [predicate].
 *
 * @param predicate the predicate that each descendant must satisfy to be included in the
 *   [Sequence].
 * @return a depth-first [Sequence] of this [PsiElement]'s descendants that satisfy the [predicate].
 * @since 1.1.0
 */
inline fun PsiElement.childrenDepthFirst(crossinline predicate: (PsiElement) -> Boolean): Sequence<PsiElement> {
  return depthFirstTraversal(this) { children.filter(predicate) }
}

/**
 * @return a breadth-first [Sequence] of this [PsiElement]'s descendants.
 * @since 1.1.0
 */
fun PsiElement.childrenBreadthFirst(): Sequence<PsiElement> {
  return breadthFirstTraversal(this) { children.toList() }
}

/**
 * Returns a breadth-first [Sequence] of all of this [PsiElement]'s descendants that satisfy the
 * specified [predicate].
 *
 * @param predicate the predicate that each descendant must satisfy to be included in the
 *   [Sequence].
 * @return a breadth-first [Sequence] of this [PsiElement]'s descendants that satisfy the
 *   [predicate].
 * @since 1.1.0
 */
inline fun PsiElement.childrenBreadthFirst(crossinline predicate: (PsiElement) -> Boolean): Sequence<PsiElement> {
  return breadthFirstTraversal(this) { children.filter(predicate) }
}

/**
 * @return `true` if this [PsiElement] is a descendant of [T], `false` otherwise.
 * @since 1.1.0
 */
inline fun <reified T : PsiElement> PsiElement.isPartOf(): Boolean {
  return getNonStrictParentOfType<T>() != null
}

/**
 * @return the [KtSimpleNameExpression] of the call name, or `null`.
 * @since 1.1.0
 */
fun KtCallElement.getCallNameExpression(): KtSimpleNameExpression? {
  val calleeExpression = calleeExpression ?: return null

  return when (calleeExpression) {
    is KtSimpleNameExpression -> calleeExpression
    is KtConstructorCalleeExpression -> calleeExpression.constructorReferenceExpression
    else -> null
  }
}

/**
 * @return the strict parent of type [T], or `null`.
 * @since 1.1.0
 */
inline fun <reified T : PsiElement> PsiElement.getStrictParentOfType(): T? {
  return PsiTreeUtil.getParentOfType(this, T::class.java, true)
}

/**
 * @return the non-strict parent of type [T], or `null`.
 * @since 1.1.0
 */
inline fun <reified T : PsiElement> PsiElement.getNonStrictParentOfType(): T? {
  return PsiTreeUtil.getParentOfType(this, T::class.java, false)
}

/**
 * @return the list of [KtParameter]s representing the value parameters.
 * @since 1.1.0
 */
fun KtNamedDeclaration.getValueParameters(): List<KtParameter> {
  return getValueParameterList()?.parameters.orEmpty()
}

/**
 * @return the [KtParameterList] of the value parameters, or `null`.
 * @since 1.1.0
 */
fun KtNamedDeclaration.getValueParameterList(): KtParameterList? {
  return when (this) {
    is KtCallableDeclaration -> valueParameterList
    is KtClass -> getPrimaryConstructorParameterList()
    else -> null
  }
}

/**
 * @return the first child of type [T], or `null`.
 * @since 1.1.0
 */
inline fun <reified T : PsiElement> PsiElement.getChildOfType(): T? {
  return PsiTreeUtil.getChildOfType(this, T::class.java)
}

/** @since 1.1.1 */
inline fun <T : PsiElement> T.removeAllChildren(shouldRemove: (PsiElement) -> Boolean = { true }): T {
  return apply {
    children
      .filter(shouldRemove)
      .forEach { it.delete() }
  }
}
