package io.foxcapades.kdbc

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Executes the receiver `PreparedStatement` using
 * [PreparedStatement.executeQuery], then calls the given [action] with the new
 * [ResultSet] as its receiver value, passing up the value returned by execution
 * of the given action function and closing the `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingPreparedStatement(sql) { ps ->
 *   ps.setInt(1, myInt)
 *
 *   ps.withResults {
 *     if (next())
 *       MyRecord(getString(1), getInt(2), getBoolean(3))
 *     else
 *       null
 *   }
 * }
 * ```
 *
 * @receiver `PreparedStatement` instance that will be executed to get a new
 * `ResultSet` instance.
 *
 * @param action Function that will be called with the new `ResultSet` instance
 * as its receiver.
 *
 * @return The value that was returned from executing the given [action].
 *
 * @see usingResults
 * @see withStatementResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> PreparedStatement.withResults(action: ResultSet.() -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return executeQuery().use(action)
}

/**
 * Executes the receiver `PreparedStatement` using
 * [PreparedStatement.executeQuery], then calls the given [action] with the new
 * [ResultSet] as its input parameter, passing up the value returned by
 * execution of the given action function and closing the `ResultSet` on return.
 *
 * Example:
 * ```kotlin
 * val record = con.usingPreparedStatement(sql) { ps ->
 *   ps.setInt(1, myInt)
 *
 *   ps.usingResults { rs ->
 *     if (next())
 *       MyRecord(rs.getString(1), rs.getInt(2), rs.getBoolean(3))
 *     else
 *       null
 *   }
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
 * @see withResults
 * @see usingStatementResults
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> PreparedStatement.usingResults(action: (ResultSet) -> T): T {
  contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
  return executeQuery().use(action)
}
