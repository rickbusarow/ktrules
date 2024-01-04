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

package com.rickbusarow.ktrules

import com.pinterest.ktlint.core.api.EditorConfigProperties
import com.pinterest.ktlint.core.api.editorconfig.CODE_STYLE_PROPERTY
import com.pinterest.ktlint.core.api.editorconfig.CodeStyleValue.android
import com.pinterest.ktlint.core.api.editorconfig.CodeStyleValue.official
import com.rickbusarow.ktrules.compat.CodeStyleValueCompat
import com.rickbusarow.ktrules.compat.EditorConfigCompat
import com.rickbusarow.ktrules.compat.EditorConfigProperty
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValue
import com.pinterest.ktlint.core.api.editorconfig.CodeStyleValue as KtLintCodeStyleValue
import com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty as KtLintEditorConfigProperty

/** @since 1.1.1 */
public class EditorConfigCompat48(
  private val properties: EditorConfigProperties
) : EditorConfigCompat {

  private val codeStyle by lazy {

    with(CODE_STYLE_PROPERTY) {
      type.getPropertyValue(type.name).parsed
        ?: defaultValue
    }
      .toCompatCodeStyleValueCompat()
  }

  override fun <T> get(editorConfigProperty: EditorConfigProperty<T>): T {

    val property = properties[editorConfigProperty.type.name]

    return if (property?.isUnset != false) {
      editorConfigProperty.defaultValue
    } else {
      editorConfigProperty.propertyMapper
        ?.invoke(property, codeStyle)
        ?: property.getValueAs()
    }
  }

  override fun <T> getEditorConfigValueOrNull(
    propertyType: PropertyType<T>,
    propertyName: String
  ): T? = properties[propertyName]?.takeIf { !it.isUnset }?.getValueAs()

  override fun contains(propertyName: String): Boolean {
    return properties.contains(propertyName)
  }

  override fun <T> map(mapper: (Property) -> T): Collection<T> {
    return properties.map { (_, property) -> mapper(property) }
  }

  private fun <T> PropertyType<T>.getPropertyValue(propertyName: String): PropertyValue<T> {
    return parse(properties[propertyName]?.sourceValue)
  }
}

/** @since 1.1.1 */
public fun <T> EditorConfigProperty<T>.toKtLintProperty48(): KtLintEditorConfigProperty<T> {
  val editorConfigProperty = this@toKtLintProperty48
  return KtLintEditorConfigProperty(
    type = editorConfigProperty.type,
    defaultValue = editorConfigProperty.defaultValue,
    propertyMapper = editorConfigProperty.propertyMapper?.let {
      { property, codeStyleValue ->
        it.invoke(property, codeStyleValue.toCompatCodeStyleValueCompat())
      }
    },
    propertyWriter = editorConfigProperty.propertyWriter,
    deprecationWarning = editorConfigProperty.deprecationWarning,
    deprecationError = editorConfigProperty.deprecationError,
    name = editorConfigProperty.name
  )
}

@Suppress("DEPRECATION")
private fun KtLintCodeStyleValue.toCompatCodeStyleValueCompat(): CodeStyleValueCompat =
  when (this) {
    android -> CodeStyleValueCompat.android
    official -> CodeStyleValueCompat.official
  }
