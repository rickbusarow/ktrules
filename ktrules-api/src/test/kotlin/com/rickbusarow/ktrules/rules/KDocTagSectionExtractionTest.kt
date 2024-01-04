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

import com.rickbusarow.ktrules.rules.internal.psi.getAllTags
import com.rickbusarow.ktrules.rules.internal.psi.getChildOfType
import com.rickbusarow.ktrules.rules.internal.psi.tagTextWithoutLeadingAsterisks
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.junit.jupiter.api.Test

class KDocTagSectionExtractionTest : Tests {

  @Test
  fun `default section paragraph and code block`() {

    val tag = kdoc(
      """
      /**
       * paragraph
       *
       *    code block
       */
       object Subject
      """
    )
      .getDefaultSection()
    val sectionText = tag.tagTextWithoutLeadingAsterisks()

    sectionText shouldBe """
      | paragraph
      |
      |    code block
    """.trimMargin()
  }

  @Test
  fun `property tag paragraph and code block`() {

    val tag = kdoc(
      """
      /**
       * @property name paragraph
       *
       *    code block
       */
       object Subject
      """
    )
      .getAllTags()
      .last()
    val sectionText = tag.tagTextWithoutLeadingAsterisks()

    sectionText shouldBe """
      | @property name paragraph
      |
      |    code block
    """.trimMargin()
  }

  @Test
  fun `unknown tags are parsed as tags`() {

    val sections = kdoc(
      """
      /**
       * First line second line
       *
       * @apple apple_link
       * @property kiwi kiwi_description
       * @orange orange_link
       * @property banana banana_description
       * @see pear
       */
       object Subject
      """
    )
      .getAllTags()
      .map { it.tagTextWithoutLeadingAsterisks() }

    sections[0] shouldBe """
        | First line second line
        |
    """.trimMargin()

    sections[1] shouldBe """
        | @apple apple_link
    """.trimMargin()

    sections[2] shouldBe """
        | @property kiwi kiwi_description
    """.trimMargin()

    sections[3] shouldBe """
        | @orange orange_link
    """.trimMargin()

    sections[4] shouldBe """
        | @property banana banana_description
    """.trimMargin()

    sections[5] shouldBe """
        | @see pear
    """.trimMargin()
  }

  fun kdoc(content: String): KDoc {
    return TestPsiFileFactory.createKotlin("subject.kt", content.trimIndent())
      .getChildOfType<KtClassOrObject>()!!
      .getChildOfType<KDoc>()!!
  }
}
