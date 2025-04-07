package io.foxcapades.kdbc

import java.sql.Connection
import java.sql.PreparedStatement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Executes the given [action] with a new [PreparedStatement] as its receiver
 * value, then closes the `PreparedStatement` and passes up the value that was
 * returned by execution of given [action] function.
 *
 * This method does not execute the `PreparedStatement`, the given [action] must
 * execute the statement in a manner of its choosing.
 *
 * Example:
 * ```kotlin
 * con.withPreparedStatement(sql) {
 *   setInt(1, myInt)
 *   setString(2, myString)
 *
 *   executeUpdate()
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * receiver.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see usingPreparedStatement
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Connection.withPreparedStatement(sql: String, action: PreparedStatement.() -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return prepareStatement(sql).use(action)
}

/**
 * Executes the given [action] with a new [PreparedStatement] as its receiver
 * value, then executes [PreparedStatement.executeUpdate], passing up the
 * returned updated row count.
 *
 * Example:
 * ```kotlin
 * val updateCount = con.withPreparedUpdate(sql) {
 *   setInt(1, myInt)
 *   setString(2, myString)
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * receiver.
 *
 * @return The updated row count.
 *
 * @see usingPreparedUpdate
 */
@OptIn(ExperimentalContracts::class)
inline fun Connection.withPreparedUpdate(sql: String, action: PreparedStatement.() -> Unit): Int {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return prepareStatement(sql).use {
    it.action()
    it.executeUpdate()
  }
}

/**
 * Creates a new [PreparedStatement] instance from the receiver [Connection],
 * then executes the given [action] for each element in [iterable] before
 * executing [PreparedStatement.executeBatch] and passing up the returned update
 * count array.
 *
 * If [executeEvery] is set to a value that is greater than zero, `executeBatch`
 * will be called between every set of records of the given count.
 *
 * If the given `iterable` contains no records, this method will not call
 * `executeBatch`.
 *
 * **NOTE**: [PreparedStatement.addBatch] is called between every execution of
 * [action], meaning `action` should not call `addBatch` itself.
 *
 * **WARNING**: [withPreparedBatchUpdate] DOES NOT call
 * [PreparedStatement.clearParameters] between calls to `action`.
 *
 * Example - Plain:
 * ```kotlin
 * val updateCounts = con.withPreparedBatchUpdate(sql, myRows) {
 *   setInt(1, it.rowId)
 *   setString(2, it.rowName)
 * }
 * ```
 *
 * Example - Execute Batch Every 500 Rows:
 * ```kotlin
 * val updateCounts = con.withPreparedBatchUpdate(sql, myRows, 500) {
 *   setInt(1, it.rowId)
 *   setString(2, it.rowName)
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param iterable Iterable stream or collection of records, on each of which
 * `action` will be called to set the params for the batch.
 *
 * @param executeEvery An optional count indicating the max number of records
 * that may be processed per call to `executeBatch`.  If set to a value that is
 * greater than zero, `executeBatch` will be called for every set of records of
 * the given size.
 *
 * Defaults to `-1`
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * receiver.
 *
 * @return The updated row count.
 *
 * @see usingPreparedBatchUpdate
 */
inline fun <T> Connection.withPreparedBatchUpdate(
  sql: String,
  iterable: Iterable<T>,
  executeEvery: Int = -1,
  action: PreparedStatement.(T) -> Unit
): IntArray =
  usingPreparedBatchUpdate(sql, iterable, executeEvery, action)

/**
 * Executes the given [action] with a new [PreparedStatement] as its input
 * parameter, then closes the `PreparedStatement` and passes up the value that
 * was returned by execution of given [action] function.
 *
 * This method does not execute the `PreparedStatement`, the given [action] must
 * execute the statement in a manner of its choosing.
 *
 * Example:
 * ```kotlin
 * con.usingPreparedStatement(sql) { ps ->
 *   ps.setInt(1, myInt)
 *   ps.setString(2, myString)
 *
 *   ps.executeUpdate()
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * input parameter.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see withPreparedStatement
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Connection.usingPreparedStatement(sql: String, action: (PreparedStatement) -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return prepareStatement(sql).use(action)
}

/**
 * Executes the given [action] with a new [PreparedStatement] as its input
 * parameter, then executes [PreparedStatement.executeUpdate], passing up the
 * returned updated row count.
 *
 * Example:
 * ```kotlin
 * val updateCount = con.usingPreparedUpdate(sql) { ps ->
 *   ps.setInt(1, myInt)
 *   ps.setString(2, myString)
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * receiver.
 *
 * @return The updated row count.
 *
 * @see withPreparedUpdate
 */
@OptIn(ExperimentalContracts::class)
inline fun Connection.usingPreparedUpdate(sql: String, action: (PreparedStatement) -> Unit): Int {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return prepareStatement(sql).use {
    action(it)
    it.executeUpdate()
  }
}

/**
 * Creates a new [PreparedStatement] instance from the receiver [Connection],
 * then executes the given [action] for each element in [iterable] before
 * executing [PreparedStatement.executeBatch] and passing up the returned update
 * count array.
 *
 * If [executeEvery] is set to a value that is greater than zero, `executeBatch`
 * will be called between every set of records of the given count.
 *
 * If the given `iterable` contains no records, this method will not call
 * `executeBatch`.
 *
 * **NOTE**: [PreparedStatement.addBatch] is called between every execution of
 * [action], meaning `action` should not call `addBatch` itself.
 *
 * **WARNING**: [withPreparedBatchUpdate] DOES NOT call
 * [PreparedStatement.clearParameters] between calls to `action`.
 *
 * Example - Plain:
 * ```kotlin
 * val updateCounts = con.withPreparedBatchUpdate(sql, myRows) {
 *   setInt(1, it.rowId)
 *   setString(2, it.rowName)
 * }
 * ```
 *
 * Example - Execute Batch Every 500 Rows:
 * ```kotlin
 * val updateCounts = con.withPreparedBatchUpdate(sql, myRows, 500) {
 *   setInt(1, it.rowId)
 *   setString(2, it.rowName)
 * }
 * ```
 *
 * @receiver The [Connection] from which a new `PreparedStatement` instance will
 * be instantiated.
 *
 * @param sql SQL string to be executed by the `PreparedStatement`.
 *
 * @param iterable Iterable stream or collection of records, on each of which
 * `action` will be called to set the params for the batch.
 *
 * @param executeEvery An optional count indicating the max number of records
 * that may be processed per call to `executeBatch`.  If set to a value that is
 * greater than zero, `executeBatch` will be called for every set of records of
 * the given size.
 *
 * Defaults to `-1`
 *
 * @param action Function to execute with the new `PreparedStatement` as its
 * receiver.
 *
 * @return The updated row count.
 *
 * @see usingPreparedBatchUpdate
 */
inline fun <T> Connection.usingPreparedBatchUpdate(
  sql: String,
  iterable: Iterable<T>,
  executeEvery: Int = -1,
  action: (PreparedStatement, T) -> Unit
): IntArray =
  prepareStatement(sql).use { ps ->
    if (executeEvery > 0) {
      val results = ArrayList<IntArray>()
      var counter = 0

      iterable.forEach {
        action(ps, it)
        ps.addBatch()

        if (++counter == executeEvery) {
          results.add(ps.executeBatch())
          counter = 0
        }
      }
      if (counter > 0)
        results.add(ps.executeBatch())

      val out = IntArray(results.sumOf { it.size })
      counter = 0
      results.forEach { a -> a.forEach { out[counter++] = it } }

      out
    } else {
      var execute = false
      iterable.forEach {
        execute = true
        action(ps, it)
      }
      if (execute)
        ps.executeBatch()
      else
        IntArray(0)
    }
  }
