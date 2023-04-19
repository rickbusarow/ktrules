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

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.api.EditorConfigOverride
import com.pinterest.ktlint.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.wrapIn
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import com.pinterest.ktlint.test.format as ktlintTestFormat
import com.pinterest.ktlint.test.lint as ktlintTestLint
import io.kotest.matchers.shouldBe as kotestShouldBe

interface Tests {

  val rules: Set<RuleProvider>
    get() = error(
      "If you need to run lint/format tests, " +
        "you must override the `rules` property in your test class."
    )

  val wrappingStyleDefault: WrappingStyle get() = WrappingStyle.MINIMUM_RAGGED
  val lineLengthDefault: Int get() = 50
  val currentVersionDefault: String get() = "0.2.3"

  infix fun String.shouldBe(expected: String) {
    dots.wrapIn("\n") kotestShouldBe expected.dots.wrapIn("\n")
  }

  fun Set<RuleProvider>.format(
    @Language("kotlin")
    text: String,
    filePath: String? = null,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    currentVersion: String = currentVersionDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue,
        PROJECT_VERSION_PROPERTY to currentVersion
      ),
  ): String = ktlintTestFormat(
    text = text.trimIndent(),
    filePath = filePath,
    editorConfigOverride = editorConfigOverride,
  )
    .first

  fun format(
    @Language("kotlin")
    text: String,
    filePath: String? = null,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    currentVersion: String = currentVersionDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue,
        PROJECT_VERSION_PROPERTY to currentVersion
      ),
    assertions: KtLintResults.() -> Unit
  ) {
    val pair = rules.ktlintTestFormat(
      text = text.trimIndent(),
      filePath = filePath,
      editorConfigOverride = editorConfigOverride,
    )

    val results = KtLintResults(output = pair.first, allLintErrors = pair.second)

    results.assertions()

    results.checkNoMoreErrors()
  }

  fun Set<RuleProvider>.lint(
    text: String,
    filePath: String? = null,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    currentVersion: String = currentVersionDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue,
        PROJECT_VERSION_PROPERTY to currentVersion
      )
  ): List<LintError> = ktlintTestLint(
    text = text.trimIndent(),
    filePath = filePath,
    editorConfigOverride = editorConfigOverride
  )

  fun lint(
    text: String,
    filePath: String? = null,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): List<LintError> = rules.ktlintTestLint(
    text = text.trimIndent(),
    filePath = filePath,
    editorConfigOverride = editorConfigOverride
  )

  fun <T> Iterable<T>.container(
    name: (T) -> String,
    action: (T) -> Iterable<DynamicNode>
  ): Stream<DynamicContainer> = map { t ->
    DynamicContainer.dynamicContainer(name(t), action(t))
  }.stream()

  fun test(name: String, action: () -> Unit): DynamicTest = DynamicTest.dynamicTest(name, action)

  fun <T> Iterable<T>.test(
    name: (T) -> String,
    action: (T) -> Unit
  ): List<DynamicTest> = map { t ->
    DynamicTest.dynamicTest(name(t)) { action(t) }
  }

  data class KtLintResults(
    val output: String,
    val allLintErrors: List<LintError>
  ) {

    private val remaining = allLintErrors.toMutableList()

    fun expectError(
      line: Int,
      col: Int,
      ruleId: String,
      detail: String,
    ) {
      val expected = LintError(line = line, col = col, ruleId = ruleId, detail = detail)

      "All errors:\n${allLintErrors.joinToString("\n")}\n\n".asClue {

        "The next error should be: $expected".asClue {
          remaining.first() kotestShouldBe expected
        }
      }

      remaining.remove(expected)
    }

    fun expectNoErrors() {
      "All errors:\n${allLintErrors.joinToString("\n")}\n\n".asClue {
        allLintErrors.shouldBeEmpty()
      }
    }

    internal fun checkNoMoreErrors() {
      remaining.shouldBeEmpty()
    }
  }
}
