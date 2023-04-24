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

package com.rickbusarow.ktrules.rules.internal.trees

import com.rickbusarow.ktrules.rules.internal.trees.AbstractTreePrinter.Color.Companion.colorized
import com.rickbusarow.ktrules.rules.internal.trees.AbstractTreePrinter.Color.Companion.noColors
import com.rickbusarow.ktrules.rules.internal.trees.AbstractTreePrinter.NameType.SIMPLE
import com.rickbusarow.ktrules.rules.internal.trees.AbstractTreePrinter.NameType.TYPE

/**
 * Base class for printing a tree structure of objects of type [T].
 *
 * @param T any type of node
 * @property whitespaceChar the character to use for replacing whitespaces in the node text when
 *   printing. Default is ' '.
 * @since 1.1.0
 */
abstract class AbstractTreePrinter<T : Any>(
  private val whitespaceChar: Char = ' '
) {
  private val elementSimpleNameMap = mutableMapOf<T, String>()
  private val elementTypeNameMap = mutableMapOf<T, String>()

  private var currentColorIndex = 0

  /**
   * @return the simple class name of an object of type [T].
   * @since 1.1.0
   */
  abstract fun T.simpleClassName(): String

  /**
   * @return the parent of an object of type [T].
   * @since 1.1.0
   */
  abstract fun T.parent(): T?

  /**
   * @return the type name of an object of type [T].
   * @since 1.1.0
   */
  abstract fun T.typeName(): String

  /**
   * @return the text representation of an object of type [T].
   * @since 1.1.0
   */
  abstract fun T.text(): String

  /**
   * @return the children of an object of type [T] as a [Sequence].
   * @since 1.1.0
   */
  abstract fun T.children(): Sequence<T>

  /**
   * Prints the tree structure of an object of type [T] to the console.
   *
   * @param [rootNode] the root node of the tree.
   * @since 1.1.0
   */
  fun printTreeString(rootNode: T) {
    println(treeString(rootNode))
  }

  /**
   * Returns the tree structure of an object of type [T] as a string.
   *
   * @param [rootNode] the root node of the tree.
   * @return the tree structure as a string.
   * @since 1.1.0
   */
  fun treeString(rootNode: T): String {
    return buildTreeString(rootNode, 0)
  }

  private fun buildTreeString(rootNode: T, indentLevel: Int): String {
    val indent = "╎  ".repeat(indentLevel)

    val thisName = rootNode.uniqueSimpleName()

    val color = getCurrentColor()

    fun String.colorized(): String {
      // return this
      return colorized(color)
    }

    val parentName = (rootNode.parent()?.uniqueSimpleName() ?: "null")
    val parentType = rootNode.parent()?.typeName() ?: "null"

    val childrenText = rootNode.children()
      .joinToString("\n") { child ->
        buildTreeString(child, indentLevel + 1)
      }

    val typeName = rootNode.typeName()

    @Suppress("MagicNumber")
    return buildString {

      val header =
        "$thisName [type: $typeName] [parent: $parentName] [parent type: $parentType]"

      val text = rootNode.text().replace(" ", "$whitespaceChar")

      val headerLength = header.countVisibleChars()

      val len = maxOf(headerLength + 4, text.lines().maxOf { it.countVisibleChars() })

      val headerBoxStart = "┏━".colorized()

      val headerBoxEnd = ("━".repeat((len - 3) - headerLength) + "┓").colorized()

      append("$indent$headerBoxStart $header $headerBoxEnd")

      append('\n')
      append(indent)
      append("┣${"━".repeat(len)}┛".colorized())
      append('\n')

      val pipe = "┃".colorized()

      val prependedText = text.prependIndent("$indent$pipe")

      append(prependedText)

      append('\n')
      append(indent)
      append("┗${"━".repeat(len)}━".colorized())

      if (childrenText.isNotEmpty()) {
        append("\n")
        append(childrenText)
      }
    }
  }

  private fun T.uniqueSimpleName(): String = uniqueName(SIMPLE)

  private fun T.uniqueName(nameType: NameType): String {
    val map = when (nameType) {
      SIMPLE -> elementSimpleNameMap
      TYPE -> elementTypeNameMap
    }

    return map.getOrPut(this@uniqueName) {
      val count = map.keys.count {
        if (nameType == SIMPLE) {
          it.simpleClassName() == simpleClassName()
        } else {
          it.typeName() == typeName()
        }
      }

      val name = if (nameType == SIMPLE) simpleClassName() else typeName()

      val unique = if (count == 0) {
        name
      } else {
        "$name (${count + 1})"
      }

      unique.colorized(getNextColor())
    }
  }

  private fun getCurrentColor(): Color = Color.values()[currentColorIndex]

  private fun getNextColor(): Color {
    currentColorIndex = (currentColorIndex + 1) % Color.values().size
    return getCurrentColor()
  }

  private fun String.countVisibleChars(): Int = noColors().length

  private enum class NameType {
    SIMPLE,
    TYPE
  }

  @Suppress("MagicNumber")
  internal enum class Color(val code: Int) {
    LIGHT_RED(91),
    LIGHT_YELLOW(93),
    LIGHT_BLUE(94),
    LIGHT_GREEN(92),
    LIGHT_MAGENTA(95),
    RED(31),
    YELLOW(33),
    BLUE(34),
    GREEN(32),
    MAGENTA(35),
    CYAN(36),
    LIGHT_CYAN(96),
    ORANGE_DARK(38),
    ORANGE_BRIGHT(48),
    PURPLE_DARK(53),
    PURPLE_BRIGHT(93),
    PINK_BRIGHT(198),
    BROWN_DARK(94),
    BROWN_BRIGHT(178),
    LIGHT_GRAY(37),
    DARK_GRAY(90),
    BLACK(30),
    WHITE(97);

    companion object {

      private val supported = "win" !in System.getProperty("os.name").lowercase()

      fun String.noColors(): String = "\u001B\\[[;\\d]*m".toRegex().replace(this, "")

      /** returns a string in the given color */
      fun String.colorized(color: Color): String {

        return if (supported) {
          "\u001B[${color.code}m$this\u001B[0m"
        } else {
          this
        }
      }
    }
  }
}
