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
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import com.pinterest.ktlint.test.format as ktlintTestFormat
import com.pinterest.ktlint.test.lint as ktlintTestLint

class KDocIndentAfterLeadingAsteriskRuleTest {

  val rules = setOf(
    RuleProvider { KDocIndentAfterLeadingAsteriskRule() }
  )

  @Test
  fun `a space is added for a default section comment`() {

    rules.format(
      """
      /**
       *comment
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * comment
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a space is added for a known tag`() {

    rules.format(
      """
      /**
       *@property name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @property name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a space is added for an unknown tag`() {

    rules.format(
      """
      /**
       *@banana name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @banana name desc
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `three spaces are added after a tag`() {

    rules.format(
      """
      /**
       * @property name desc
       *second line
       * third line
       *  fourth line
       *   fifth line
       *    sixth line
       *
       *second paragraph
       *
       * third paragraph
       *
       *  fourth paragraph
       *
       *   fifth paragraph
       *
       *    sixth paragraph
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @property name desc
       *   second line
       *   third line
       *   fourth line
       *   fifth line
       *    sixth line
       *
       *   second paragraph
       *
       *   third paragraph
       *
       *   fourth paragraph
       *
       *   fifth paragraph
       *
       *    sixth paragraph
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a space is added for a collapsed default section comment`() {

    rules.format(
      """
      /**comment */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /** comment */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  private fun Set<RuleProvider>.format(
    @Language("kotlin")
    text: String,
    editorConfigOverride: EditorConfigOverride = EditorConfigOverride.EMPTY_EDITOR_CONFIG_OVERRIDE
  ): String = ktlintTestFormat(
    text = text,
    filePath = null,
    editorConfigOverride = editorConfigOverride,
  )
    .first

  private fun Set<RuleProvider>.lint(
    @Language("kotlin")
    text: String,
    editorConfigOverride: EditorConfigOverride = EditorConfigOverride.EMPTY_EDITOR_CONFIG_OVERRIDE
  ): List<LintError> = ktlintTestLint(
    text = text,
    editorConfigOverride = editorConfigOverride
  )
}
