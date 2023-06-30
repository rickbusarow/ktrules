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

package com.rickbusarow.ktrules.compat

import org.ec4j.core.model.PropertyType

private const val MAX_LINE_LENGTH_PROPERTY_ANDROID_STUDIO_CODE_STYLE = 100
private const val MAX_LINE_LENGTH_PROPERTY_KTLINT_OFFICIAL_CODE_STYLE = 140
private const val MAX_LINE_LENGTH_PROPERTY_OFF_EDITOR_CONFIG = "off"

@Suppress("DEPRECATION")
private fun CodeStyleValueCompat.defaultValue() = when (this) {
  CodeStyleValueCompat.android -> MAX_LINE_LENGTH_PROPERTY_ANDROID_STUDIO_CODE_STYLE
  CodeStyleValueCompat.android_studio -> MAX_LINE_LENGTH_PROPERTY_ANDROID_STUDIO_CODE_STYLE
  CodeStyleValueCompat.official -> MAX_LINE_LENGTH_PROPERTY_OFF
  CodeStyleValueCompat.intellij_idea -> MAX_LINE_LENGTH_PROPERTY_OFF
  CodeStyleValueCompat.ktlint_official -> MAX_LINE_LENGTH_PROPERTY_KTLINT_OFFICIAL_CODE_STYLE
}

private var isInvalidValueLoggedBefore = false

/**
 * Integer value that denotes that the property is to be considered as disabled.
 *
 * @since 1.1.1
 */
const val MAX_LINE_LENGTH_PROPERTY_OFF: Int = Int.MAX_VALUE

/**
 * This property moves
 *
 * @since 1.1.0
 */
internal val MAX_LINE_LENGTH_PROPERTY: EditorConfigProperty<Int>
  get() = EditorConfigProperty(
    name = PropertyType.max_line_length.name,
    type = PropertyType.max_line_length,
    defaultValue = MAX_LINE_LENGTH_PROPERTY_OFF,
    androidStudioCodeStyleDefaultValue = MAX_LINE_LENGTH_PROPERTY_ANDROID_STUDIO_CODE_STYLE,
    intellijIdeaCodeStyleDefaultValue = MAX_LINE_LENGTH_PROPERTY_OFF,
    ktlintOfficialCodeStyleDefaultValue = MAX_LINE_LENGTH_PROPERTY_KTLINT_OFFICIAL_CODE_STYLE,
    propertyMapper = { property, codeStyleValue ->
      when {
        property == null || property.isUnset -> {
          codeStyleValue.defaultValue()
        }

        /**
         * Internally, Ktlint uses integer 'Int.MAX_VALUE' to indicate that the
         * max line length has to be ignored as this is easier in comparisons
         * to check whether the maximum length of a line is exceeded.
         *
         * @since 1.1.1
         */
        property.sourceValue ==
          MAX_LINE_LENGTH_PROPERTY_OFF_EDITOR_CONFIG -> MAX_LINE_LENGTH_PROPERTY_OFF

        else ->
          PropertyType
            .max_line_length
            .parse(property.sourceValue)
            .let {
              if (!it.isValid) {
                if (!isInvalidValueLoggedBefore) {
                  isInvalidValueLoggedBefore = true
                }
                if (it.source == "-1") {
                  MAX_LINE_LENGTH_PROPERTY_OFF
                } else {
                  codeStyleValue.defaultValue()
                }
              } else {
                it.parsed
              }
            }
      }
    },
    propertyWriter = { property ->
      if (property <= 0 || property == MAX_LINE_LENGTH_PROPERTY_OFF) {
        MAX_LINE_LENGTH_PROPERTY_OFF_EDITOR_CONFIG
      } else {
        property.toString()
      }
    }
  )
