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
import com.rickbusarow.ktrules.rules.internal.psi.getCallNameExpression
import com.rickbusarow.ktrules.rules.internal.psi.ktPsiFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument

/**
 * Ensures calls to Gradle's `withType(...)` use `configureEach { }` for the lambda.
 *
 * @since 1.0.9
 */
class NoWithTypeWithLambdaRule : RuleCompat(ID) {

  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    if (node.elementType != ElementType.CALL_EXPRESSION) return

    val callExpression = node.psi as KtCallExpression

    if (callExpression.getCallNameExpression()?.text != "withType") return

    val dotExpression = callExpression.parent as? KtDotQualifiedExpression
      ?: return

    val lambdaArg = callExpression.valueArguments
      .firstOrNull { it is KtLambdaArgument } as? KtLambdaArgument
      ?: return

    emit(
      callExpression.textOffset,
      ERROR_MESSAGE,
      true
    )

    if (autoCorrect) {

      callExpression.node.removeChild(lambdaArg.node)

      val newExpressionText = buildString {
        append(dotExpression.text.trim())
        // If the call used to be `foo.withType<Bar> { ... }`, then we have to add parenthesis
        if (callExpression.valueArgumentList == null) {
          append("()")
        }
        append(".configureEach ")
        // Re-use the lambda text.
        append(lambdaArg.text)
      }

      val newExpression = callExpression.ktPsiFactory().createExpression(newExpressionText)

      dotExpression.parent.node.addChild(newExpression.node, dotExpression.node)
      dotExpression.parent.node.removeChild(dotExpression.node)
    }
  }

  internal companion object {
    val ID = RuleId("no-gradle-with-type-with-lambda")
    const val ERROR_MESSAGE = "Use 'configureEach' instead of passing a lambda to 'withType'"
  }
}
