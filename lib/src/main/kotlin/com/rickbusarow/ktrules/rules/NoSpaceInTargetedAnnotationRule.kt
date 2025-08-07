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

import com.rickbusarow.ktrules.compat.ElementType
import com.rickbusarow.ktrules.compat.RuleCompat
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.rules.internal.psi.children
import com.rickbusarow.ktrules.rules.internal.psi.isWhiteSpace
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Ensures that there's no whitespace before or after the colon in a targeted annotation.
 *
 * ```
 * // Bad code
 * @file: Suppress("DEPRECATION")
 * @file :Suppress("DEPRECATION")
 *
 * // good code
 * @file:Suppress("DEPRECATION")
 * ```
 *
 * @since 1.0.1
 */
class NoSpaceInTargetedAnnotationRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(node: ASTNode, emit: EmitWithDecision) {

    if (node.elementType == ElementType.ANNOTATION_ENTRY) {

      val whitespace = node.children().firstOrNull { it.isWhiteSpace() } ?: return

      emit(node.startOffset, ERROR_MESSAGE, true)
      node.removeChild(whitespace)
    }
  }

  internal companion object {
    val ID = RuleId("no-space-in-annotation-with-target")
    const val ERROR_MESSAGE = "no space after annotation target"
  }
}
