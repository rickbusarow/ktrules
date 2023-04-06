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
import com.rickbusarow.ktrules.rules.internal.dots
import com.rickbusarow.ktrules.rules.internal.wrapIn
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import com.pinterest.ktlint.test.format as ktlintTestFormat
import com.pinterest.ktlint.test.lint as ktlintTestLint
import io.kotest.matchers.shouldBe as kotestShouldBe

interface Tests {

  infix fun String.shouldBe(expected: String) {
    dots.wrapIn("\n") kotestShouldBe expected.dots.wrapIn("\n")
  }

  fun Set<RuleProvider>.format(
    @Language("kotlin")
    text: String,
    editorConfigOverride: EditorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride
  ): String = ktlintTestFormat(
    text = text,
    lintedFilePath = null,
    editorConfigOverride = editorConfigOverride,
  )

  fun Set<RuleProvider>.lint(
    @Language("kotlin")
    text: String,
    editorConfigOverride: EditorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride
  ): List<LintError> = ktlintTestLint(
    text = text,
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
}
