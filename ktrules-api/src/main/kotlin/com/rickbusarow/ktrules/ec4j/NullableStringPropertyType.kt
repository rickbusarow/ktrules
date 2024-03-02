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

package com.rickbusarow.ktrules.ec4j

import org.ec4j.core.model.PropertyType

/**
 * Returns a value of `null` if a value is not defined, instead of the default
 * EC4J behavior which would return an actual String with value of `"null"`.
 *
 * @since 1.1.2
 */
internal class NullableStringPropertyType(
  name: String?,
  description: String?,
  vararg possibleValues: String?
) : PropertyType<String?>(name, description, null, *possibleValues) {

  override fun parse(value: String?): PropertyValue<String?> {
    return if (value == "null") {
      PropertyValue.valid(null, value)
    } else {
      PropertyValue.valid(value, value)
    }
  }
}
