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

package com.rickbusarow.ktrules.ec4j

import com.rickbusarow.ktrules.compat.EditorConfigProperty
import com.rickbusarow.ktrules.rules.ec4j.NullableStringPropertyType
import com.rickbusarow.ktrules.rules.internal.WrappingStyle.Companion.WRAPPING_STYLE_PROPERTY
import com.rickbusarow.ktrules.rules.internal.WrappingStyle.Companion.WRAPPING_STYLE_PROPERTY_DEPRECATED

internal const val RULES_PREFIX = "ktlint_kt-rules"

@Suppress("DEPRECATION")
internal val ALL_PROPERTIES by lazy {
  setOf(
    PROJECT_VERSION_PROPERTY_DEPRECATED,
    PROJECT_VERSION_PROPERTY,
    WRAPPING_STYLE_PROPERTY_DEPRECATED,
    WRAPPING_STYLE_PROPERTY
  )
}

/**
 * Returns a valid `null` value if the property isn't set
 *
 * @since 1.1.2
 */
@Deprecated("to be removed in the next release.  Use `PROJECT_VERSION_PROPERTY` instead.")
internal val PROJECT_VERSION_PROPERTY_DEPRECATED: EditorConfigProperty<String?>
  get() = NullableStringPropertyType(
    "${RULES_PREFIX}_project_version",
    "the current project version as a literal string"
  ).let { type ->

    EditorConfigProperty(
      name = type.name,
      type = type,
      defaultValue = null,
      deprecationError = "Use just kt-rules_project_version instead",
      propertyMapper = { property, _ ->
        property?.sourceValue?.trim('"', '\'')
      }
    )
  }

/**
 * Returns a valid `null` value if the property isn't set
 *
 * @since 1.1.4
 */
internal val PROJECT_VERSION_PROPERTY: EditorConfigProperty<String?>
  get() = NullableStringPropertyType(
    // don't prefix this with `ktlint_` because it's not a rule ID.
    // KtLint's parsing will fail if it sees this editorconfig property.
    name = "kt-rules_project_version",
    description = "the current project version as a literal string"
  ).let { type ->

    EditorConfigProperty(
      name = type.name,
      type = type,
      defaultValue = null,
      propertyMapper = { property, _ ->
        property?.sourceValue?.trim('"', '\'')
      }
    )
  }
