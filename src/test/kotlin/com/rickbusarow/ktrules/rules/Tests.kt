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

import com.rickbusarow.ktrules.compat.Code
import com.rickbusarow.ktrules.compat.EditorConfigOverride
import com.rickbusarow.ktrules.compat.KtLintRuleEngine
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.compat.RuleProvider
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.wrapIn
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
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
    script: Boolean = false,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    currentVersion: String = currentVersionDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue,
        PROJECT_VERSION_PROPERTY to currentVersion
      ),
  ): String = KtLintRuleEngine(
    ruleProviders = rules,
    editorConfigOverride = editorConfigOverride
  ).format(
    Code.fromSnippet(text.trimIndent(), script = script)
  )

  fun format(
    @Language("kotlin")
    text: String,
    script: Boolean = false,
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
    val errors = mutableListOf<KtLintResults.Error>()
    val outputString = KtLintRuleEngine(
      ruleProviders = rules,
      editorConfigOverride = editorConfigOverride
    ).format(
      Code.fromSnippet(text.trimIndent(), script = script)
    ) { lintError, corrected ->
      errors.add(
        KtLintResults.Error(
          line = lintError.line,
          col = lintError.col,
          ruleId = lintError.ruleId.value,
          detail = lintError.detail,
          corrected = corrected
        )
      )
    }

    val results = KtLintResults(output = outputString, allLintErrors = errors)

    results.assertions()

    results.checkNoMoreErrors()
  }

  fun Set<RuleProvider>.lint(
    text: String,
    script: Boolean = false,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    currentVersion: String = currentVersionDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue,
        PROJECT_VERSION_PROPERTY to currentVersion
      )
  ): List<KtLintResults.Error> = buildList {
    KtLintRuleEngine(
      ruleProviders = rules,
      editorConfigOverride = editorConfigOverride
    ).lint(
      Code.fromSnippet(text.trimIndent(), script = script)
    ) { lintError ->
      add(
        KtLintResults.Error(
          line = lintError.line,
          col = lintError.col,
          ruleId = lintError.ruleId.value,
          detail = lintError.detail,
          corrected = false
        )
      )
    }
  }

  fun lint(
    text: String,
    script: Boolean = false,
    wrappingStyle: WrappingStyle = wrappingStyleDefault,
    lineLength: Int = lineLengthDefault,
    editorConfigOverride: EditorConfigOverride =
      EditorConfigOverride.from(
        MAX_LINE_LENGTH_PROPERTY to lineLength,
        WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue
      )
  ): List<KtLintResults.Error> = buildList {
    KtLintRuleEngine(
      ruleProviders = rules,
      editorConfigOverride = editorConfigOverride
    ).lint(
      Code.fromSnippet(text.trimIndent(), script = script)
    ) { lintError ->
      add(
        KtLintResults.Error(
          line = lintError.line,
          col = lintError.col,
          ruleId = lintError.ruleId.value,
          detail = lintError.detail,
          corrected = false
        )
      )
    }
  }

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
    val allLintErrors: List<Error>
  ) {

    private val remaining = allLintErrors.toMutableList()

    fun expectError(
      line: Int,
      col: Int,
      ruleId: RuleId,
      detail: String,
      corrected: Boolean = true
    ) {
      expectError(
        line = line,
        col = col,
        ruleId = ruleId.value,
        detail = detail,
        corrected = corrected
      )
    }

    fun expectError(
      line: Int,
      col: Int,
      ruleId: String,
      detail: String,
      corrected: Boolean = true
    ) {
      val expected = Error(
        line = line,
        col = col,
        ruleId = ruleId,
        detail = detail,
        corrected = corrected
      )

      "All errors:\n${allLintErrors.joinToString("\n")}\n\n".asClue {

        "The next error should be: $expected".asClue {
          remaining.firstOrNull() kotestShouldBe expected
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
      "All errors:\n${allLintErrors.joinToString("\n")}\n\n".asClue {
        remaining.shouldBeEmpty()
      }
    }

    data class Error(
      val line: Int,
      val col: Int,
      val ruleId: String,
      val detail: String,
      val corrected: Boolean
    )
  }
}
