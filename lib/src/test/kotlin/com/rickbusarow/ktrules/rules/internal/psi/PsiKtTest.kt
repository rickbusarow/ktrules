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
package com.rickbusarow.ktrules.rules.internal.psi

import com.rickbusarow.ktrules.rules.Tests
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PsiKtTest : Tests {

  @Nested
  inner class `fileIndent` {

    @Test
    fun `fileIndent should return correct number of spaces`() {
      val psiFile = createKotlin(
        "MyFile.kt",
        """
        |fun main() {
        |  println("Hello, World!")
        |}
        """.trimMargin()
      )
      val psiElement = psiFile.findElementAt(psiFile.text.indexOf("println"))
      val result = psiElement?.fileIndent(0)

      result shouldBe "  "
    }

    @Test
    fun `fileIndent should add additional offset to the indentation`() {
      val psiFile = createKotlin(
        "MyFile.kt",
        """
        |fun main() {
        |  println("Hello, World!")
        |}
        """.trimMargin()
      )
      val psiElement = psiFile.findElementAt(psiFile.text.indexOf("println"))
      val result = psiElement?.fileIndent(2)

      result shouldBe "    "
    }

    @Test
    fun `fileIndent should return only additional offset if element is at line start`() {
      val psiFile = createKotlin(
        "MyFile.kt",
        """
        |fun main() {
        |  println("Hello, World!")
        |}
        """.trimMargin()
      )
      val psiElement = psiFile.findElementAt(psiFile.text.indexOf("fun main"))
      val result = psiElement?.fileIndent(3)

      result shouldBe "   "
    }

    @Test
    fun `fileIndent should return empty string if no indentation and no additional offset`() {
      val psiFile = createKotlin(
        "MyFile.kt",
        """
        |fun main() {
        |  println("Hello, World!")
        |}
        """.trimMargin()
      )
      val psiElement = psiFile.findElementAt(psiFile.text.indexOf("fun main"))
      val result = psiElement?.fileIndent(0)

      result shouldBe ""
    }
  }
}
