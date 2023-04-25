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

package com.rickbusarow.ktrules

import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.rickbusarow.ktrules.compat.CodeStyleValueCompat
import com.rickbusarow.ktrules.compat.EditorConfigCompat
import com.rickbusarow.ktrules.compat.EditorConfigProperty
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue as KtLintCodeStyleValue
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty as KtLintEditorConfigProperty

/** */
class EditorConfigCompat49(private val delegate: EditorConfig) : EditorConfigCompat {
  override fun <T> get(editorConfigProperty: EditorConfigProperty<T>): T {
    return delegate[editorConfigProperty.toKtLintProperty49()]
  }

  override fun <T> getEditorConfigValueOrNull(
    propertyType: PropertyType<T>,
    propertyName: String
  ): T? = delegate.getEditorConfigValueOrNull(propertyType, propertyName)

  override fun contains(propertyName: String): Boolean {
    return delegate.contains(propertyName)
  }

  override fun <T> map(mapper: (Property) -> T): Collection<T> {
    return delegate.map(mapper)
  }
}

/** */
fun <T> EditorConfigProperty<T>.toKtLintProperty49(): KtLintEditorConfigProperty<T> {
  val editorConfigProperty = this@toKtLintProperty49

  return KtLintEditorConfigProperty(
    type = editorConfigProperty.type,
    defaultValue = editorConfigProperty.defaultValue,
    ktlintOfficialCodeStyleDefaultValue = editorConfigProperty.ktlintOfficialCodeStyleDefaultValue,
    intellijIdeaCodeStyleDefaultValue = editorConfigProperty.intellijIdeaCodeStyleDefaultValue,
    androidStudioCodeStyleDefaultValue = editorConfigProperty.androidStudioCodeStyleDefaultValue,
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
    KtLintCodeStyleValue.android -> CodeStyleValueCompat.android
    KtLintCodeStyleValue.android_studio -> CodeStyleValueCompat.android_studio
    KtLintCodeStyleValue.intellij_idea -> CodeStyleValueCompat.intellij_idea
    KtLintCodeStyleValue.ktlint_official -> CodeStyleValueCompat.ktlint_official
    KtLintCodeStyleValue.official -> CodeStyleValueCompat.official
  }
