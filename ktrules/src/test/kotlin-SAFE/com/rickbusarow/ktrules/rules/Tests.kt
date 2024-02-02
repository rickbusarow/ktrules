/*
 * Copyright (C) 2024 Rick Busarow
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

import com.rickbusarow.ktrules.KtRulesRuleSetProvider
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.rules.internal.WrappingStyle
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.wrapIn
import io.kotest.assertions.asClue
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.psi.KtFile
import io.kotest.matchers.shouldBe as kotestShouldBe

interface Tests<ECO> : HasDynamicTests {

  val ruleProviders: Set<RuleProviderCompat>
    get() = error(
      "If you need to run lint/format tests, " +
        "you must override the `ruleProviders` property in your test class."
    )

  val wrappingStyleDefault: WrappingStyle get() = WrappingStyle.MINIMUM_RAGGED
  val lineLengthDefault: Int get() = 50
  val currentVersionDefault: String get() = "0.2.3"

  infix fun String?.shouldBe(expected: String) {
    this?.dots?.wrapIn("\n") kotestShouldBe expected.dots.wrapIn("\n")
  }

  fun format(
    @Language("kotlin")
    text: String,
    rules: Set<RuleProviderCompat> = ruleProviders,
    script: Boolean = false,
    includeAllRules: Boolean = false,
    lineLength: Int? = lineLengthDefault,
    wrappingStyle: WrappingStyle? = wrappingStyleDefault,
    currentVersion: String? = currentVersionDefault,
    assertions: KtLintTestResult.() -> Unit
  ) {
    format(
      text = text,
      script = script,
      includeAllRules = includeAllRules,
      editorConfig = EditorConfig.from(
        lineLength = lineLength,
        wrappingStyle = wrappingStyle,
        currentVersion = currentVersion
      ),
      assertions = assertions
    )
  }

  fun format(
    @Language("kotlin")
    text: String,
    rules: Set<RuleProviderCompat> = ruleProviders,
    script: Boolean = false,
    includeAllRules: Boolean = false,
    editorConfig: EditorConfig<ECO> = EditorConfig.from(
      lineLength = lineLengthDefault,
      wrappingStyle = wrappingStyleDefault,
      currentVersion = currentVersionDefault
    ),
    assertions: KtLintTestResult.() -> Unit
  )

  fun lint(
    text: String,
    rules: Set<RuleProviderCompat> = ruleProviders,
    script: Boolean = false,
    includeAllRules: Boolean = false,
    lineLength: Int? = lineLengthDefault,
    wrappingStyle: WrappingStyle? = wrappingStyleDefault,
    currentVersion: String? = currentVersionDefault
  ): List<KtLintTestResult.LintError> = lint(
    text = text,
    script = script,
    includeAllRules = includeAllRules,
    editorConfig = EditorConfig.from(
      lineLength = lineLength,
      wrappingStyle = wrappingStyle,
      currentVersion = currentVersion
    )
  )

  fun lint(
    text: String,
    rules: Set<RuleProviderCompat> = ruleProviders,
    script: Boolean = false,
    includeAllRules: Boolean = false,
    editorConfig: EditorConfig<ECO>?
  ): List<KtLintTestResult.LintError>

  sealed interface EditorConfig<ECO> {

    data class Properties<ECO>(
      val lineLength: Int?,
      val wrappingStyle: WrappingStyle?,
      val currentVersion: String?
    ) : EditorConfig<ECO>

    data class Override<ECO>(
      /*EditorConfigOverride*/
      val editorConfigOverride: ECO
    ) : EditorConfig<ECO>

    data class File<ECO>(@Language("editorconfig") val content: String) : EditorConfig<ECO>

    companion object
  }

  fun EditorConfig.Companion.from(
    lineLength: Int? = lineLengthDefault,
    wrappingStyle: WrappingStyle? = wrappingStyleDefault,
    currentVersion: String? = currentVersionDefault
  ): EditorConfig.Properties<ECO> = EditorConfig.Properties(
    lineLength = lineLength,
    wrappingStyle = wrappingStyle,
    currentVersion = currentVersion
  )

  fun EditorConfig.Companion.from(
    @Language("editorconfig") content: String
  ): EditorConfig.File<ECO> = EditorConfig.File(content)

  fun createKotlin(
    name: String,
    @Language("kotlin")
    content: String
  ): KtFile = TestPsiFileFactory.createKotlin(name = name, content = content)

  data class KtLintTestResult(
    val output: String,
    val allLintErrors: List<KtLintTestResult.LintError>
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
      val expected = LintError(
        line = line,
        col = col,
        ruleId = "${KtRulesRuleSetProvider.ID}:$ruleId",
        detail = detail,
        corrected = corrected
      )

      withClue {

        "The next error should be: $expected".asClue {
          remaining.firstOrNull() kotestShouldBe expected
        }
      }

      remaining.remove(expected)
    }

    fun expectNoErrors() {
      withClue {
        allLintErrors.shouldBeEmpty()
      }
    }

    fun checkNoMoreErrors() {
      withClue {
        remaining.shouldBeEmpty()
      }
    }

    private inline fun withClue(action: () -> Unit) {
      withClue(clue = { this@KtLintTestResult.toString() }, thunk = action)
    }

    override fun toString(): String {
      return """==== KtLintTestResult
        |
        | -- output with interpuncts
        |${output.dots}
        |
        | -- output without interpuncts
        |$output
        |
        | -- all errors:
        |${allLintErrors.joinToString("\n")}
        |
        | -- remaining errors:
        |${remaining.joinToString("\n")}
        |""".replaceIndentByMargin()
    }

    data class LintError(
      val line: Int,
      val col: Int,
      val ruleId: String,
      val detail: String,
      val corrected: Boolean
    )
  }
}
