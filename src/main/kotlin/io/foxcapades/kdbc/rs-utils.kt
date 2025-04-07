package io.foxcapades.kdbc

import java.sql.Array
import java.sql.ResultSet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Inline wrapper function around [ResultSet.getObject] that uses the generic
 * type [T] to populate the class value for the second argument.
 *
 * Example 1 - Type is already known:
 * ```kotlin
 * data class Foo(val something: String)
 *
 * Foo(something = rs.get(1))
 * ```
 *
 * Example 2 - Type is provided to call:
 * ```kotlin
 * val foo = rs.get<String>(1)
 * ```
 *
 * @param columnIndex Index of the column in the current result row from which
 * the value should be retrieved.
 *
 * @return The value from the result row at the given column index, retrieved
 * as type [T].
 */
inline operator fun <reified T> ResultSet.get(columnIndex: Int): T =
  getObject(columnIndex, T::class.java)

/**
 * Inline wrapper function around [ResultSet.getObject] that uses the generic
 * type [T] to populate the class value for the second argument.
 *
 * Example 1 - Type is already known:
 * ```kotlin
 * data class Foo(val something: String)
 *
 * Foo(something = rs.get("something"))
 * ```
 *
 * Example 2 - Type is provided to call:
 * ```kotlin
 * val foo = rs.get<String>("foo")
 * ```
 *
 * @param columnLabel Label for the column in the current result row from which
 * the value should be retrieved.
 *
 * @return The value from the result row at the given column name, retrieved as
 * type [T].
 */
inline operator fun <reified T> ResultSet.get(columnLabel: String): T =
  getObject(columnLabel, T::class.java)

/**
 * Iterates through the rows in the receiver [ResultSet] instance, and builds a
 * list of the values computed for each row by the given function.
 *
 * The [map] function handles calling [ResultSet.next] to iterate through rows.
 * If the provided input function calls `ResultSet` cursor indexing methods, it
 * may result in a number of list entries that differs from the number of rows
 * in the `ResultSet`.
 *
 * This function _DOES NOT_ close the receiver `ResultSet`.
 *
 * Example:
 * ```kotlin
 * // List<String>
 * val result = rs.map {
 *   it.getString(1)
 * }
 * ```
 *
 * @receiver The `ResultSet` whose rows will be transformed into elements in the
 * output list.
 *
 * @param initialCapacity Initial size of the output list.  If the result size
 * is known ahead of time, this value may be provided to reduce memory
 * reallocations as the output list is constructed.
 *
 * Defaults to `10`.  See `java.util.ArrayList#DEFAULT_CAPACITY`.
 *
 * @param fn Function used to transform each `ResultSet` row into a value of
 * type [T] for addition to the returned `List`.
 *
 * @return A `List` containing the computed values for the result rows in the
 * receiver `ResultSet`.
 */
inline fun <T> ResultSet.map(initialCapacity: Int = 10, fn: (ResultSet) -> T): List<T> {
  val out = ArrayList<T>(initialCapacity)

  while (next())
    out.add(fn(this))

  return out
}

/**
 * Iterates through the rows in the receiver [ResultSet] instance, and executes
 * the given function for each row.
 *
 * The [forEach] function handles calling [ResultSet.next] to iterate through
 * rows.  If the provided input function calls `ResultSet` cursor indexing
 * methods, it may result in rows being skipped or processed multiple times.
 *
 * This function _DOES NOT_ close the receiver `ResultSet`.
 *
 * @receiver The `ResultSet` whose rows will be iterated over.
 *
 * @param fn Function that will be passed the `ResultSet` instance for each
 * result row.
 */
inline fun ResultSet.forEach(fn: (ResultSet) -> Any?) {
  while (next())
    fn(this)
}

/**
 * Iterates through the rows in the receiver [ResultSet] instance, and builds a
 * collection of type [C] containing the values computed for each row by the
 * given transform function.
 *
 * The [mapInto] function handles calling [ResultSet.next] to iterate through
 * rows.  If the provided input function calls `ResultSet` cursor indexing
 * methods, it may result in a number of collection elements that differs from
 * the number of rows in the `ResultSet`.
 *
 * This function _DOES NOT_ close the receiver `ResultSet`.
 *
 * Example:
 * ```kotlin
 * rs.mapInto(HashSet<String>(32)) {
 *   it.getString(1)
 * }
 * ```
 *
 * @receiver The `ResultSet` whose rows will be transformed into elements in the
 * output collection.
 *
 * @param tgt Target collection of type [C] that will have transformed result
 * rows added to it.
 *
 * @param fn Function used to transform each `ResultSet` row into a value of
 * type [T] for addition to the returned collection.
 *
 * @return The input collection [tgt].
 */
inline fun <T, C: MutableCollection<T>> ResultSet.mapInto(tgt: C, fn: (ResultSet) -> T): C {
  while (next())
    tgt.add(fn(this))
  return tgt
}

/**
 * Iterates through the rows in the receiver [ResultSet] instance, and builds a
 * map from the key/value [Pair]s returned by the given transform function.
 *
 * The [toMap] function handles calling [ResultSet.next] to iterate through
 * rows.  If the provided input function calls `ResultSet` cursor indexing
 * methods, it may result in a number of map entries that differs from the
 * number of rows in the `ResultSet`.
 *
 * This function _DOES NOT_ close the receiver `ResultSet`.
 *
 * Example 1:
 * ```kotlin
 * // Map<String, OffsetDateTime>
 * val result = rs.toMap {
 *   it.getString("key") to it.get<OffsetDateTime>("created_on")
 * }
 * ```
 *
 * Example 2:
 * ```kotlin
 * val result = rs.toMap(HashMap<Int, String>(16)) {
 *   it.getInt("id") to it.getString("label")
 * }
 * ```
 *
 * @receiver The `ResultSet` whose rows will be transformed into entries in the
 * output map.
 *
 * @param map Map instance that will be populated with the transformed keys and
 * values from the given transform function.
 *
 * Defaults to an instance of [LinkedHashMap].
 *
 * @param fn Function used to transform `ResultSet` rows into key/value pairs
 * for insertion into the output map.
 *
 * @return The given map value [map].
 */
inline fun <K, V> ResultSet.toMap(map: MutableMap<K, V> = LinkedHashMap(), fn: (ResultSet) -> Pair<K, V>): Map<K, V> {
  while (next())
    with(fn(this)) { map.put(first, second) }
  return map
}

/**
 * Iterates through the rows in the receiver [ResultSet] instance, and builds a
 * map from the keys and values returned by the given transform functions.
 *
 * The [toMap] function handles calling [ResultSet.next] to iterate through
 * rows.  If the provided input function calls `ResultSet` cursor indexing
 * methods, it may result in a number of map entries that differs from the
 * number of rows in the `ResultSet`.
 *
 * This function _DOES NOT_ close the receiver `ResultSet`.
 *
 * Example 1 - Function Refs:
 * ```kotlin
 * val result = rs.toMap(keyFn = ::myKeyGetter, valFn = ::myValGetter)
 * ```
 *
 * Example 2 - Half & Half:
 * ```kotlin
 * val result = rs.toMap(keyFn = ::myKeyGetter) {
 *   it.getInt(2)
 * }
 * ```
 *
 * Example 3 - Curly Braces:
 * ```kotlin
 * val result = rs.toMap(keyFn = { it.getInt(1) }, valFn = { it.getString(2) })
 * ```
 *
 * Example 4 - Bring Your Own Map:
 * ```kotlin
 * val result = rs.toMap(HashMap<String, String>(), ::myKeyGetter, ::myValGetter)
 * ```
 *
 * @receiver The `ResultSet` whose rows will be transformed into entries in the
 * output map.
 *
 * @param map Map instance that will be populated with the transformed keys and
 * values from the given transform function.
 *
 * Defaults to an instance of [LinkedHashMap].
 *
 * @param keyFn Function used to transform `ResultSet` rows into map entry keys.
 *
 * @param valFn Function used to transform `ResultSet` rows into map entry
 * values.
 *
 * @return The given map value [map].
 */
inline fun <K, V> ResultSet.toMap(
  map: MutableMap<K, V> = LinkedHashMap(),
  keyFn: (ResultSet) -> K,
  valFn: (ResultSet) -> V
): Map<K, V> {
  while(next())
    map[keyFn(this)] = valFn(this)
  return map
}

/**
 * Creates a [Sequence] that iterates over the rows in the receiver [ResultSet],
 * yielding that `ResultSet` for each result row.
 *
 * The generated `Sequence` handles calling [ResultSet.next] to iterate through
 * rows.  If functions chained from the returned sequence call `ResultSet`
 * cursor indexing, it may result in a number of yielded rows that differs from
 * the actual result row count.
 *
 * The returned `Sequence` _DOES NOT_ close the receiver `ResultSet`.  The
 * `ResultSet` should be closed only _after_ the sequence has been consumed,
 * returning the `Sequence` from a closing block will result in undefined
 * behavior, likely an exception.
 *
 * Example:
 * ```kotlin
 * rs.toSequence()
 *   .filter { isAcceptable(it.getString(1), it.getBigDecimal(5)) }
 *   .map { it.getInt(3) to MyObj(it.getString(2), it.getBoolean(4)) }
 *   .toMap()
 * ```
 *
 * @receiver The `ResultSet` whose rows will be yielded to the sequence.
 *
 * @return A sequence over the receiver `ResultSet` rows.
 */
fun ResultSet.toSequence() = sequence {
  while(next())
    yield(this@toSequence)
}

/**
 * Executes the given transform function with a target JDBC [Array] as its
 * receiver value.
 *
 * The given function [fn] will be executed against an `Array` instance
 * retrieved from the given column index on the current result row. The value
 * returned by this transform function will be passed up as the return value of
 * [withArray].
 *
 * The `Array` resources will be freed by `withArray` on return by use of the
 * [Array.free] method.
 *
 * @receiver `ResultSet` containing the `Array` value to transform.
 *
 * @param columnIndex Index of the `Array` value in the current result row.
 *
 * @param fn Transform function used to process the JDBC `Array` value into a
 * return value of type [T].
 *
 * @return The result of calling the transform function [fn].
 *
 * @see usingArray
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> ResultSet.withArray(columnIndex: Int, fn: Array.() -> T): T {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
  val arr = getArray(columnIndex)
  return try {
    fn(arr)
  } finally {
    arr.free()
  }
}

/**
 * Executes the given transform function with a target JDBC [Array] as its
 * input parameter value.
 *
 * The given function [fn] will be executed against an `Array` instance
 * retrieved from the given column index on the current result row. The value
 * returned by this transform function will be passed up as the return value of
 * [usingArray].
 *
 * The `Array` resources will be freed by `usingArray` on return by use of the
 * [Array.free] method.
 *
 * @receiver `ResultSet` containing the `Array` value to transform.
 *
 * @param columnIndex Index of the `Array` value in the current result row.
 *
 * @param fn Transform function used to process the JDBC `Array` value into a
 * return value of type [T].
 *
 * @return The result of calling the transform function [fn].
 *
 * @see withArray
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> ResultSet.usingArray(columnIndex: Int, fn: (Array) -> T): T {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
  val arr = getArray(columnIndex)
  return try {
    fn(arr)
  } finally {
    arr.free()
  }
}

/**
 * Executes the given transform function with a target JDBC [Array] as its
 * receiver value.
 *
 * The given function [fn] will be executed against an `Array` instance
 * retrieved from the column with the given label on the current result row. The
 * value returned by this transform function will be passed as the return value
 * of [withArray].
 *
 * The `Array` resources will be freed by `withArray` on return by use of the
 * [Array.free] method.
 *
 * @receiver `ResultSet` containing the `Array` value to transform.
 *
 * @param columnLabel Name of the column containing the target `Array` value in
 * the current result row.
 *
 * @param fn Transform function used to process the JDBC `Array` value into a
 * return value of type [T].
 *
 * @return The result of calling the transform function [fn].
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> ResultSet.withArray(columnLabel: String, fn: Array.() -> T): T {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
  val arr = getArray(columnLabel)
  return try {
    fn(arr)
  } finally {
    arr.free()
  }
}

/**
 * Executes the given transform function with a target JDBC [Array] as its
 * input parameter value.
 *
 * The given function [fn] will be executed against an `Array` instance
 * retrieved from the column with the given label on the current result row. The
 * value returned by this transform function will be passed up as the return
 * value of [usingArray].
 *
 * The `Array` resources will be freed by `usingArray` on return by use of the
 * [Array.free] method.
 *
 * @receiver `ResultSet` containing the `Array` value to transform.
 *
 * @param columnLabel Name of the column containing the target `Array` value in
 * the current result row.
 *
 * @param fn Transform function used to process the JDBC `Array` value into a
 * return value of type [T].
 *
 * @return The result of calling the transform function [fn].
 *
 * @see withArray
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> ResultSet.usingArray(columnLabel: String, fn: (Array) -> T): T {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }
  val arr = getArray(columnLabel)
  return try {
    fn(arr)
  } finally {
    arr.free()
  }
}
