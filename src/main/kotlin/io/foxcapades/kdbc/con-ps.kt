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
