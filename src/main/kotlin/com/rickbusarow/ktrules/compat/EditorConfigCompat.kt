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
 * Loaded [Property]s from `.editorconfig` files.
 *
 * @since 1.1.1
 */
interface EditorConfigCompat {

  /**
   * Gets the value of [editorConfigProperty] from [EditorConfigCompat] provided that
   * the name of the property is identical to the name of the type of the property. If
   * the value is not found, the default value for the matching code style is returned.
   *
   * @since 1.1.1
   */
  operator fun <T> get(editorConfigProperty: EditorConfigProperty<T>): T

  /**
   * Gets the value of the property with [propertyType] and name
   * [propertyName] from [EditorConfigCompat]. Returns null if the type is
   * not found. Also, returns null when no property with the name is found.
   *
   * @since 1.1.1
   */
  fun <T> getEditorConfigValueOrNull(propertyType: PropertyType<T>, propertyName: String): T?

  /**
   * Checks whether a property with name [propertyName] is defined.
   *
   * @since 1.1.1
   */
  fun contains(propertyName: String): Boolean

  /**
   * Maps all properties with given [mapper] to a collection.
   *
   * @since 1.1.1
   */
  fun <T> map(mapper: (Property) -> T): Collection<T>
}
