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

package com.rickbusarow.ktrules.rules.internal

import com.rickbusarow.ktrules.compat.EditorConfigProperty
import com.rickbusarow.ktrules.ec4j.RULES_PREFIX
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.LowerCasingPropertyType
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Represents the available algorithms for wrapping text. Each algorithm has different output.
 *
 * @property displayValue A human-readable representation of the wrapping style.
 * @since 1.0.8
 */
public enum class WrappingStyle(public val displayValue: String) {
  /**
   * The GREEDY wrapping style aims to fill each line with as many words as
   * possible, minimizing the number of lines. This approach might result
   * in uneven line lengths, but its output is usually more predictable.
   *
   * @since 1.0.8
   */
  GREEDY("greedy"),

  /**
   * The MINIMUM_RAGGED wrapping style tries to create lines with roughly equal lengths,
   * reducing the raggedness of the text. This approach may have less predictable output
   * compared to the GREEDY style, but often leads to a more visually appealing result.
   *
   * @since 1.0.8
   */
  MINIMUM_RAGGED("equal");

  public companion object {
    /**
     * @see WrappingStyle
     * @since 1.1.2
     */
    @Deprecated("Use `kt-rules_wrapping_style` instead.")
    public val WRAPPING_STYLE_PROPERTY_DEPRECATED: EditorConfigProperty<WrappingStyle> by lazy(
      NONE
    ) {
      val wrappingStylePropertyType: LowerCasingPropertyType<WrappingStyle> =
        LowerCasingPropertyType(
          "${RULES_PREFIX}_wrapping_style",
          values().map { it.displayValue }.toString(),
          PropertyType.PropertyValueParser.EnumValueParser(WrappingStyle::class.java),
          values().mapToSet { it.displayValue }
        )

      EditorConfigProperty(
        name = wrappingStylePropertyType.name,
        type = wrappingStylePropertyType,
        defaultValue = MINIMUM_RAGGED,
        deprecationError = "Use `kt-rules_wrapping_style` instead.",
        propertyMapper = { property, _ ->

          val name = property?.sourceValue?.trim('"', '\'')?.lowercase()

          values().firstOrNull { it.displayValue == name }
        },
        propertyWriter = { it.displayValue }
      )
    }

    /**
     * @see WrappingStyle
     * @since 1.1.2
     */
    public val WRAPPING_STYLE_PROPERTY: EditorConfigProperty<WrappingStyle> by lazy(NONE) {
      val wrappingStylePropertyType: LowerCasingPropertyType<WrappingStyle> =
        LowerCasingPropertyType(
          "kt-rules_wrapping_style",
          values().map { it.displayValue }.toString(),
          PropertyType.PropertyValueParser.EnumValueParser(WrappingStyle::class.java),
          values().mapToSet { it.displayValue }
        )

      EditorConfigProperty(
        name = wrappingStylePropertyType.name,
        type = wrappingStylePropertyType,
        defaultValue = MINIMUM_RAGGED,
        propertyMapper = { property, _ ->

          val name = property?.sourceValue?.trim('"', '\'')?.lowercase()

          values().firstOrNull { it.displayValue == name }
        },
        propertyWriter = { it.displayValue }
      )
    }
  }
}
