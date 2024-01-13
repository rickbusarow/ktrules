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

package com.rickbusarow.ktrules

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride.Companion
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.rickbusarow.ktrules.compat.EditorConfigProperty
import com.rickbusarow.ktrules.compat.MAX_LINE_LENGTH_PROPERTY
import com.rickbusarow.ktrules.compat.RuleProviderCompat
import com.rickbusarow.ktrules.ec4j.PROJECT_VERSION_PROPERTY
import com.rickbusarow.ktrules.rules.Tests
import com.rickbusarow.ktrules.rules.internal.WrappingStyle
import com.rickbusarow.ktrules.rules.internal.letIf
import com.rickbusarow.ktrules.rules.internal.mapToSet
import org.intellij.lang.annotations.Language
import java.nio.file.Files
import java.util.ServiceLoader

interface TestsCompat : Tests<EditorConfigOverride> {

  fun EditorConfigOverride.Companion.from(
    vararg properties: Pair<EditorConfigProperty<*>, *>
  ): EditorConfigOverride {
    return from(
      *properties.map { it.first.toKtLintProperty() to it.second }.toTypedArray()
    )
  }

  fun Set<RuleProviderCompat>.toKtLintRuleProviders(): Set<RuleProvider> {
    return mapToSet { it.toKtLintRuleProvider() }
  }

  override fun format(
    @Language("kotlin")
    text: String,
    rules: Set<RuleProviderCompat>,
    script: Boolean,
    includeAllRules: Boolean,
    editorConfig: Tests.EditorConfig<EditorConfigOverride>,
    assertions: Tests.KtLintTestResult.() -> Unit
  ) {
    val errors = mutableListOf<Tests.KtLintTestResult.LintError>()

    val outputString = withEngine(
      ruleProviders = rules,
      editorConfig = editorConfig,
      includeAllRules = includeAllRules,
      action = {
        format(
          Code.fromSnippet(
            text.trimIndent(),
            script = script
          )
        ) { lintError, corrected ->
          errors.add(
            Tests.KtLintTestResult.LintError(
              line = lintError.line,
              col = lintError.col,
              ruleId = lintError.ruleId.value,
              detail = lintError.detail,
              corrected = corrected
            )
          )
        }
      }
    )

    with(Tests.KtLintTestResult(output = outputString, allLintErrors = errors)) {
      assertions()
      checkNoMoreErrors()
    }
  }

  override fun lint(
    text: String,
    rules: Set<RuleProviderCompat>,
    script: Boolean,
    includeAllRules: Boolean,
    editorConfig: Tests.EditorConfig<EditorConfigOverride>?
  ): List<Tests.KtLintTestResult.LintError> = buildList {
    withEngine(
      ruleProviders = rules,
      editorConfig = editorConfig,
      includeAllRules = includeAllRules,
      action = fun KtLintRuleEngine.() {
        lint(
          Code.fromSnippet(content = text.trimIndent(), script = script)
        ) { lintError ->
          add(
            Tests.KtLintTestResult.LintError(
              line = lintError.line,
              col = lintError.col,
              ruleId = lintError.ruleId.value,
              detail = lintError.detail,
              corrected = false
            )
          )
        }
      }
    )
  }

  fun <T> withEngine(
    ruleProviders: Set<RuleProviderCompat>,
    editorConfig: Tests.EditorConfig<EditorConfigOverride>?,
    includeAllRules: Boolean,
    action: KtLintRuleEngine.() -> T
  ): T {

    return Jimfs.newFileSystem(Configuration.forCurrentPlatform()).use { fileSystem ->

      val rules =
        ruleProviders.toKtLintRuleProviders()
          .letIf(includeAllRules) {
            this + ServiceLoader.load(RuleSetProviderV3::class.java)
              .flatMap { it.getRuleProviders() }
          }

      val editorConfigOverride = when (editorConfig) {
        null -> null
        is Tests.EditorConfig.File -> {
          val path = fileSystem.getPath(".editorconfig")
          Files.write(path, editorConfig.content.toByteArray())
          null
        }

        is Tests.EditorConfig.Override -> editorConfig.editorConfigOverride
        is Tests.EditorConfig.Properties -> createEditorConfigOverride(
          editorConfig.lineLength,
          editorConfig.wrappingStyle,
          editorConfig.currentVersion
        )
      }

      val engine = if (editorConfigOverride != null) {
        KtLintRuleEngine(
          ruleProviders = rules,
          editorConfigOverride = editorConfigOverride,
          fileSystem = fileSystem
        )
      } else {
        KtLintRuleEngine(
          ruleProviders = rules,
          fileSystem = fileSystem
        )
      }

      engine.action()
    }
  }

  fun createEditorConfigOverride(
    lineLength: Int?,
    wrappingStyle: WrappingStyle?,
    currentVersion: String?
  ): EditorConfigOverride {
    val pairs = buildList {
      if (lineLength != null) {
        add(MAX_LINE_LENGTH_PROPERTY to lineLength)
      }
      if (wrappingStyle != null) {
        add(WrappingStyle.WRAPPING_STYLE_PROPERTY to wrappingStyle.displayValue)
      }
      if (currentVersion != null) {
        add(PROJECT_VERSION_PROPERTY to currentVersion)
      }
    }

    return EditorConfigOverride.from(*pairs.toTypedArray())
  }
}
