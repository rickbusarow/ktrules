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

package com.rickbusarow.ktrules.rules.internal

/**
 * shorthand for `mapNotNull { ... }.single()`
 *
 * @since 1.1.1
 */
inline fun <T, R : Any> Iterable<T>.singleNotNullOf(transform: (T) -> R?): R {
  return mapNotNull(transform).single()
}

/**
 * shorthand for `mapTo(mutableSetOf()) { ... }`
 *
 * @since 1.1.1
 */
inline fun <C : Iterable<T>, T, R> C.mapToSet(
  destination: MutableSet<R> = mutableSetOf(),
  transform: (T) -> R
): MutableSet<R> = mapTo(destination, transform)

/**
 * shorthand for `mapTo(mutableSetOf()) { ... }`
 *
 * @since 1.1.1
 */
inline fun <T, R> Array<T>.mapToSet(
  destination: MutableSet<R> = mutableSetOf(),
  transform: (T) -> R
): MutableSet<R> = mapTo(destination, transform)

/**
 * shorthand for `flatMapTo(mutableSetOf()) { ... }`
 *
 * @since 1.1.1
 */
inline fun <T, R> Iterable<T>.flatMapToSet(
  destination: MutableSet<R> = mutableSetOf(),
  transform: (T) -> Iterable<R>
): MutableSet<R> = flatMapTo(destination, transform)

/**
 * shorthand for `mapNotNull { ... }.single()`
 *
 * @since 1.1.1
 */
fun <T, R : Any> Sequence<T>.singleNotNullOf(transform: (T) -> R?): R {
  return mapNotNull(transform).single()
}

/**
 * shorthand for `sequenceOf(*elements).filterNotNull()`
 *
 * @since 1.1.1
 */
fun <T : Any> sequenceOfNotNull(vararg elements: T?): Sequence<T> =
  sequenceOf(*elements).filterNotNull()

/**
 * returns [defaultValue] if the receiver is null or is empty
 *
 * @since 1.1.1
 */
inline fun <C, R> C?.ifNullOrEmpty(defaultValue: () -> R): R where R : Collection<*>,
                                                                   C : R {
  return if (isNullOrEmpty()) defaultValue() else this
}

/**
 * A Sequence which only yields each element once, but is
 * **not** constrained to only one consumer via [constrainOnce].
 *
 * Given this code:
 *
 * ```
 * val seq = sequence {
 *   repeat(5) { num ->
 *     println("    yield $num")
 *     yield(num)
 *   }
 * }
 *   .stateful()
 *
 * repeat(3) {
 *   println(seq.firstOrNull { num -> num % 3 == 0 })
 * }
 * ```
 *
 * output:
 *
 * ```text
 *      yield 0
 * 0
 *      yield 1
 *      yield 2
 *      yield 3
 * 3
 *      yield 4
 * null
 * ```
 *
 * @since 1.1.1
 */
fun <T> Sequence<T>.stateful(): Sequence<T> {
  val iterator = iterator()
  return Sequence { iterator }
}

/**
 * Returns a list of all elements sorted according to the specified [selectors].
 *
 * The sort is _stable_. It means that equal elements
 * preserve their order relative to each other after sorting.
 *
 * @since 1.0.4
 */
fun <T> Iterable<T>.sortedWith(vararg selectors: (T) -> Comparable<*>): List<T> {
  if (this is Collection) {
    if (size <= 1) return this.toList()
    @Suppress("UNCHECKED_CAST")
    return (toTypedArray<Any?>() as Array<T>).apply { sortWith(compareBy(*selectors)) }.asList()
  }
  return toMutableList().apply { sortWith(compareBy(*selectors)) }
}
