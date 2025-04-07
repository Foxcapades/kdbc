package io.foxcapades.kdbc

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Executes the receiver `Statement` using [Statement.executeQuery], then calls
 * the given [action] with the new [ResultSet] as its receiver value, passing up
 * the value returned by execution of the given action function and closing the
 * `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingStatementResults(sql) {
 *   if (next())
 *     MyRecord(getString(1), getInt(2), getBoolean(3))
 *   else
 *     null
 * }
 * ```
 *
 * @receiver `Statement` instance that will be executed to get a new `ResultSet`
 * instance.
 *
 * @param action Function that will be called with the new `ResultSet` instance
 * as its receiver.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see usingStatementResults
 * @see withResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Statement.withStatementResults(sql: String, action: ResultSet.() -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return executeQuery(sql).use(action)
}

/**
 * Executes the receiver `Statement` using [Statement.executeQuery], then calls
 * the given [action] with the new [ResultSet] as its input parameter, passing
 * up the value returned by execution of the given action function and closing
 * the `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingStatementResults(sql) { rs ->
 *   if (next())
 *     MyRecord(rs.getString(1), rs.getInt(2), rs.getBoolean(3))
 *   else
 *     null
 * }
 * ```
 *
 * @receiver `PreparedStatement` instance that will be executed to get a new
 * `ResultSet` instance.
 *
 * @param action Function that will be called with the new `ResultSet` instance
 * as its input parameter.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see withStatementResults
 * @see usingResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Statement.usingStatementResults(sql: String, action: (ResultSet) -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return executeQuery(sql).use(action)
}

/**
 * Creates a new [Statement] using [Connection.createStatement], then executes
 * the new `Statement` using [Statement.executeQuery], calling the given
 * [action] with the returned [ResultSet] as its receiver value, passing up
 * the value returned by execution of the given action function and closing the
 * `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingStatementResults(sql) {
 *   if (next())
 *     MyRecord(getString(1), getInt(2), getBoolean(3))
 *   else
 *     null
 * }
 * ```
 *
 * @receiver `Connection` instance from which a new `Statement` will be created.
 *
 * @param action Function that will be called with the new `ResultSet` instance
 * as its receiver.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see usingStatementResults
 * @see withResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Connection.withStatementResults(sql: String, action: ResultSet.() -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return createStatement().executeQuery(sql).use(action)
}

/**
 * Creates a new [Statement] using [Connection.createStatement], then executes
 * the new `Statement` using [Statement.executeQuery], calling the given
 * [action] with the returned [ResultSet] as its receiver value, passing up
 * the value returned by execution of the given action function and closing the
 * `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingStatementResults(sql) {
 *   if (next())
 *     MyRecord(getString(1), getInt(2), getBoolean(3))
 *   else
 *     null
 * }
 * ```
 *
 * @receiver `Connection` instance from which a new `Statement` will be created.
 *
 * @param action Function that will be called with the new `ResultSet` instance
 * as its input argument.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see usingStatementResults
 * @see withResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Connection.usingStatementResults(sql: String, action: (ResultSet) -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return createStatement().executeQuery(sql).use(action)
}
