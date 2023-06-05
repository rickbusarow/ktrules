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

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.rickbusarow.ktrules.KtRulesRuleSetProvider
import com.rickbusarow.ktrules.compat.Code
import com.rickbusarow.ktrules.compat.EditorConfigOverride
import com.rickbusarow.ktrules.compat.EditorConfigProperty
import com.rickbusarow.ktrules.compat.KtLintRuleEngine
import com.rickbusarow.ktrules.compat.MAX_LINE_LENGTH_PROPERTY
import com.rickbusarow.ktrules.compat.RuleId
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.compat.from
import com.rickbusarow.ktrules.compat.toKtLintRuleProviders49
import com.rickbusarow.ktrules.ec4j.PROJECT_VERSION_PROPERTY
import com.rickbusarow.ktrules.rules.internal.WrappingStyle
import com.rickbusarow.ktrules.rules.internal.WrappingStyle.Companion.WRAPPING_STYLE_PROPERTY
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.wrapIn
import io.kotest.assertions.asClue
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.ServiceLoader
import java.util.stream.Stream
import io.kotest.matchers.shouldBe as kotestShouldBe

interface Tests {

  val ruleProviders: Set<RuleProviderCompat>
    get() = error(
      "If you need to run lint/format tests, " +
        "you must override the `ruleProviders` property in your test class."
    )

  val wrappingStyleDefault: WrappingStyle get() = WrappingStyle.MINIMUM_RAGGED
  val lineLengthDefault: Int get() = 50
  val currentVersionDefault: String get() = "0.2.3"

  infix fun String.shouldBe(expected: String) {
    dots.wrapIn("\n") kotestShouldBe expected.dots.wrapIn("\n")
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
      editorConfigOverride = createEditorConfigOverride(
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
    editorConfigOverride: EditorConfigOverride,
    assertions: KtLintTestResult.() -> Unit
  ) {
    val errors = mutableListOf<KtLintTestResult.Error>()

    val outputString = withEngine(
      ruleProviders = rules,
      editorConfigOverride = editorConfigOverride,
      includeAllRules = includeAllRules
    ) {
      format(Code.fromSnippet(text.trimIndent(), script = script)) { lintError, corrected ->
        errors.add(
          KtLintTestResult.Error(
            line = lintError.line,
            col = lintError.col,
            ruleId = lintError.ruleId.value,
            detail = lintError.detail,
            corrected = corrected
          )
        )
      }
    }

    with(KtLintTestResult(output = outputString, allLintErrors = errors)) {
      assertions()
      checkNoMoreErrors()
    }
  }

  fun lint(
    text: String,
    rules: Set<RuleProviderCompat> = ruleProviders,
    script: Boolean = false,
    includeAllRules: Boolean = false,
    lineLength: Int? = lineLengthDefault,
    wrappingStyle: WrappingStyle? = wrappingStyleDefault,
    currentVersion: String? = currentVersionDefault
  ): List<KtLintTestResult.Error> = lint(
    text = text,
    script = script,
    includeAllRules = includeAllRules,
    editorConfigOverride = createEditorConfigOverride(
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
    editorConfigOverride: EditorConfigOverride
  ): List<KtLintTestResult.Error> = buildList {
    withEngine(rules, editorConfigOverride, includeAllRules) {
      lint(Code.fromSnippet(content = text.trimIndent(), script = script)) { lintError ->
        add(
          KtLintTestResult.Error(
            line = lintError.line,
            col = lintError.col,
            ruleId = lintError.ruleId.value,
            detail = lintError.detail,
            corrected = false
          )
        )
      }
    }
  }

  fun <T> withEngine(
    ruleProviders: Set<RuleProviderCompat>,
    editorConfigOverride: EditorConfigOverride,
    includeAllRules: Boolean,
    action: KtLintRuleEngine.() -> T
  ): T {

    return Jimfs.newFileSystem(Configuration.forCurrentPlatform()).use { fileSystem ->
      val engine = KtLintRuleEngine(
        ruleProviders = ruleProviders.toKtLintRuleProviders49()
          .letIf(includeAllRules) {
            plus(
              ServiceLoader.load(RuleSetProviderV3::class.java)
                .flatMap { it.getRuleProviders() }
            )
          },
        editorConfigOverride = editorConfigOverride,
        fileSystem = fileSystem
      )

      engine.action()
    }
  }

  fun createEditorConfigOverride(
    lineLength: Int?,
    wrappingStyle: WrappingStyle?,
    currentVersion: String?,
    vararg otherPairs: Pair<EditorConfigProperty<*>, *>
  ): EditorConfigOverride {
    val pairs = buildList {
      if (lineLength != null) {
        add(MAX_LINE_LENGTH_PROPERTY to lineLength)
      }
      if (wrappingStyle != null) {
        add(WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue)
      }
      if (currentVersion != null) {
        add(PROJECT_VERSION_PROPERTY to currentVersion)
      }
      addAll(otherPairs)
    }

    return EditorConfigOverride.from(*pairs.toTypedArray())
  }

  fun <T> Iterable<T>.container(
    name: (T) -> String,
    action: (T) -> Iterable<DynamicNode>
  ): Stream<DynamicContainer> = map { t ->
    DynamicContainer.dynamicContainer(name(t), action(t))
  }.stream()

  fun test(name: String, action: () -> Unit): DynamicTest = DynamicTest.dynamicTest(name, action)

  fun <T> Iterable<T>.test(name: (T) -> String, action: (T) -> Unit): List<DynamicTest> = map { t ->
    DynamicTest.dynamicTest(name(t)) { action(t) }
  }

  data class KtLintTestResult(
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

    internal fun checkNoMoreErrors() {
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

    data class Error(
      val line: Int,
      val col: Int,
      val ruleId: String,
      val detail: String,
      val corrected: Boolean
    )
  }
}
