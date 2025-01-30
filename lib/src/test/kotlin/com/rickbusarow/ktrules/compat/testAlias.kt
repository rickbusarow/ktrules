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

package com.rickbusarow.ktrules.compat

import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.rickbusarow.ktrules.rules.internal.mapToSet
import com.rickbusarow.ktrules.toKtLintProperty140
import com.rickbusarow.ktrules.toKtLintRuleProvider140

fun EditorConfigOverride.Companion.from(
  vararg properties: Pair<EditorConfigProperty<*>, *>
): EditorConfigOverride {
  return EditorConfigOverride.from(
    *properties.map { it.first.toKtLintProperty140() to it.second }.toTypedArray()
  )
}

fun Set<RuleProviderCompat>.toKtLintRuleProviders140(): Set<RuleProvider> {
  return mapToSet { it.toKtLintRuleProvider140() }
}
