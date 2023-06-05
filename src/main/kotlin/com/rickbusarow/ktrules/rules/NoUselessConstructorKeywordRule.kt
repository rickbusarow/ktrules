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

import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.psi.parent
import com.rickbusarow.ktrules.rules.internal.psi.prevLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtPrimaryConstructor

/**
 * Ensures that there's no unnecessary `constructor` keyword in class declarations.
 *
 * ```
 * // Bad code
 * class MyClass constructor(val foo: String)
 *
 * // good code
 * class MyClass(val foo: String)
 * ```
 *
 * @since 1.0.1
 */
class NoUselessConstructorKeywordRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.CONSTRUCTOR_KEYWORD) {
      val constructorNode = node.parent(ElementType.PRIMARY_CONSTRUCTOR) ?: return

      val constructorPsi = constructorNode.psi as? KtPrimaryConstructor ?: return

      if (constructorPsi.annotations.isEmpty() && constructorPsi.modifierList == null) {

        emit(node.startOffset, ERROR_MESSAGE, true)
        val leadingWhitespaceNode = node.prevLeaf(true)!!
        constructorNode.removeChild(node)
        constructorNode.removeChild(leadingWhitespaceNode)
      }
    }
  }

  internal companion object {
    val ID = RuleId("no-useless-constructor-keyword")
    const val ERROR_MESSAGE: String = "Useless constructor keyword"
  }
}
