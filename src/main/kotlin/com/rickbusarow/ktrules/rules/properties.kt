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

import com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty
import org.ec4j.core.model.PropertyType.LowerCasingPropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser
import org.ec4j.core.model.PropertyType.PropertyValueParser.EnumValueParser

internal const val RULES_PREFIX = "ktlint_kt-rules"

internal val ALL_PROPERTIES by lazy {
  setOf(
    PROJECT_VERSION_PROPERTY,
    WRAPPING_STYLE_PROPERTY
  )
}

internal val PROJECT_VERSION_PROPERTY: EditorConfigProperty<String?> by lazy {

  val projectVersionPropertyType = LowerCasingPropertyType(
    "${RULES_PREFIX}_project_version",
    "the current project version as a literal string",
    PropertyValueParser.IDENTITY_VALUE_PARSER
  )

  EditorConfigProperty(
    name = projectVersionPropertyType.name,
    type = projectVersionPropertyType,
    defaultValue = null,
    defaultAndroidValue = null,
    propertyMapper = { property, _ ->
      property?.sourceValue?.trim('"', '\'')
    },
  )
}

/** Represents the available algorithms for wrapping text. Each algorithm has different output.
 * @property displayValue A human-readable representation of the wrapping style.
 * @since 1.0.8
 */
enum class WrappingStyle(val displayValue: String) {
  /**
   * The GREEDY wrapping style aims to fill each line with as many words as possible, minimizing the
   * number of lines. This approach might result in uneven line lengths, but its output is usually
   * more predictable.
   *
   * @since 1.0.8
   */
  GREEDY("greedy"),

  /**
   * The MINIMUM_RAGGED wrapping style tries to create lines with roughly equal lengths, reducing
   * the raggedness of the text. This approach may have less predictable output compared to the
   * GREEDY style, but often leads to a more visually appealing result.
   *
   * @since 1.0.8
   */
  MINIMUM_RAGGED("equal")
}

internal val WRAPPING_STYLE_PROPERTY: EditorConfigProperty<WrappingStyle> by lazy {

  val wrappingStylePropertyType = LowerCasingPropertyType(
    "${RULES_PREFIX}_wrapping_style",
    WrappingStyle.values().map { it.displayValue }.toString(),
    EnumValueParser(WrappingStyle::class.java),
    WrappingStyle.values().mapTo(mutableSetOf()) { it.displayValue }
  )

  EditorConfigProperty(
    name = wrappingStylePropertyType.name,
    type = wrappingStylePropertyType,
    defaultValue = WrappingStyle.MINIMUM_RAGGED,
    defaultAndroidValue = WrappingStyle.MINIMUM_RAGGED,
    propertyMapper = { property, _ ->

      val name = property?.sourceValue?.trim('"', '\'')

      WrappingStyle.values().firstOrNull { it.displayValue == name }
    },
  )
}
