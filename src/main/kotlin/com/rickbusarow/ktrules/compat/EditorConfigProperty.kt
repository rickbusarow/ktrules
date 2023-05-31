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

import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType

/**
 * Definition of '.editorconfig' property enriched with fields required by the
 * [com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine].
 *
 * @since 1.1.1
 */
data class EditorConfigProperty<T>(
  /**
   * Type of property. Could be one of default ones (see [PropertyType.STANDARD_TYPES]) or custom
   * one.
   *
   * @since 1.1.1
   */
  val type: PropertyType<T>,
  /**
   * Default value for property if it does not exist in loaded properties. This default applies to
   * all code styles unless the code style specific default value is set.
   *
   * @since 1.1.1
   */
  val defaultValue: T,
  /**
   * Default value for property if it does not exist in loaded properties and codestyle
   * 'ktlint_official'. When not set, it is defaulted to [defaultValue].
   *
   * @since 1.1.1
   */
  val ktlintOfficialCodeStyleDefaultValue: T = defaultValue,
  /**
   * Default value for property if it does not exist in loaded properties and codestyle
   * 'intellij_idea'. When not set, it is defaulted to [defaultValue].
   *
   * @since 1.1.1
   */
  val intellijIdeaCodeStyleDefaultValue: T = defaultValue,
  /**
   * Default value for property if it does not exist in loaded properties and codestyle
   * 'android_studio'. When not set, it is defaulted to [defaultValue].
   *
   * @since 1.1.1
   */
  val androidStudioCodeStyleDefaultValue: T = defaultValue,
  /**
   * If set, it maps the actual value set for the property, to another valid value for that
   * property. See example below where
   * ```kotlin
   * propertyMapper = { property, isAndroidCodeStyle ->
   *     when {
   *         property == null ->
   *             // property is not defined in ".editorconfig" file
   *         property.isUnset ->
   *             // property is defined in ".editorconfig" file with special value "unset"
   *         property.sourceValue == "some-string-value" ->
   *             // property is defined in ".editorconfig" file with a value that needs to be remapped to another
   *             // valid value. For example the "max_line_length" property accepts value "off" but is remapped to
   *             // "-1" in ktlint.
   *        else ->
   *             property.getValueAs() // or null
   *     }
   * }
   * ```
   * In case the lambda returns a null value then, the code style specific default value or the
   * generic [defaultValue] is used.
   *
   * @since 1.1.1
   */
  val propertyMapper: ((Property?, CodeStyleValueCompat) -> T?)? = null,
  /**
   * Custom function that represents [T] as String. Defaults to the standard `toString()` call.
   * Override the default implementation in case you need a different behavior than the standard
   * `toString()` (e.g. for collections joinToString() is more applicable).
   *
   * @since 1.1.1
   */
  val propertyWriter: (T) -> String = { it.toString() },
  /**
   * Optional message to be displayed whenever the value of the property is being retrieved while it
   * has been deprecated.
   *
   * @since 1.1.1
   */
  val deprecationWarning: String? = null,
  /**
   * Optional message to be displayed whenever the value of the property is being retrieved while it
   * has been deprecated.
   *
   * @since 1.1.1
   */
  val deprecationError: String? = null,
  /**
   * Name of the property. A property must be named in case multiple properties are defined for the
   * same type. Defaults to the name of the type when not set.
   *
   * @since 1.1.1
   */
  val name: String = type.name
)
