package io.foxcapades.kdbc

import java.math.BigInteger
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.SQLType
import java.sql.Types

private val UnsignedLongMask = BigInteger.ONE.shiftLeft(Long.SIZE_BITS) - BigInteger.ONE

/**
 * Sets the given [value] on the receiver [PreparedStatement] at the given
 * [index] as a [Short] (SQL type [`SMALLINT`][Types.SMALLINT]) value using
 * [PreparedStatement.setShort].
 *
 * Value is set as the `SMALLINT` type so as not to cause unexpected behavior
 * for values exceeding the max signed `int8` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UByte] value at.
 *
 * @param value Value to set at the given index.
 */
fun PreparedStatement.setUByte(index: Int, value: UByte) =
  setShort(index, value.toShort())

/**
 * Sets the given [value] on the receiver [PreparedStatement] at the given
 * [index] as an [Int] (SQL type [`INTEGER`][Types.INTEGER]) value using
 * [PreparedStatement.setInt].
 *
 * Value is set as the `INTEGER` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int16` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UShort] value at.
 *
 * @param value Value to set at the given index.
 */
fun PreparedStatement.setUShort(index: Int, value: UShort) =
  setInt(index, value.toInt())

/**
 * Sets the given [value] on the receiver [PreparedStatement] at the given
 * [index] as a [Long] value (SQL type [`BIGINT`][Types.BIGINT]) using
 * [PreparedStatement.setLong].
 *
 * Value is set as the `BIGINT` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int32` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UInt] value at.
 *
 * @param value Value to set at the given index.
 */
fun PreparedStatement.setUInt(index: Int, value: UInt) =
  setLong(index, value.toLong())

/**
 * Sets the given [value] on the receiver [PreparedStatement] at the given
 * [index] as a [BigDecimal] (SQL type [`NUMERIC`][Types.NUMERIC]) value using
 * [PreparedStatement.setBigDecimal].
 *
 * Value is set as the `NUMERIC` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int64` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [ULong] value at.
 *
 * @param value Value to set at the given index.
 */
fun PreparedStatement.setULong(index: Int, value: ULong) =
  setBigDecimal(index, BigInteger.valueOf(value.toLong()).and(UnsignedLongMask).toBigDecimal())

/**
 * Indexed setter for the given [value] on the receiver [PreparedStatement]
 * which casts the value as a [Short] (SQL type [`SMALLINT`][Types.SMALLINT]).
 *
 * Value is set as the `SMALLINT` type so as not to cause unexpected behavior
 * for values exceeding the max signed `int8` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UByte] value at.
 *
 * @param value Value to set at the given index.
 */
operator fun PreparedStatement.set(index: Int, value: UByte) =
  setShort(index, value.toShort())

/**
 * Indexed setter for the given [value] on the receiver [PreparedStatement]
 * which casts the value as a [Int] (SQL type [`INTEGER`][Types.INTEGER]).
 *
 * Value is set as the `INTEGER` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int16` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UShort] value at.
 *
 * @param value Value to set at the given index.
 */
operator fun PreparedStatement.set(index: Int, value: UShort) =
  setInt(index, value.toInt())

/**
 * Indexed setter for the given [value] on the receiver [PreparedStatement]
 * which casts the value as a [Long] (SQL type [`BIGINT`][Types.BIGINT]).
 *
 * Value is set as the `BIGINT` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int32` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [UInt] value at.
 *
 * @param value Value to set at the given index.
 */
operator fun PreparedStatement.set(index: Int, value: UInt) =
  setLong(index, value.toLong())

/**
 * Indexed setter for the given [value] on the receiver [PreparedStatement]
 * which casts the value as a [BigDecimal]
 * (SQL type [`NUMERIC`][Types.NUMERIC]).
 *
 * Value is set as the `NUMERIC` type so as not to cause unexpected behavior for
 * values exceeding the max signed `int64` value.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given [ULong] value at.
 *
 * @param value Value to set at the given index.
 */
operator fun PreparedStatement.set(index: Int, value: ULong) =
  setBigDecimal(index, BigInteger.valueOf(value.toLong()).and(UnsignedLongMask).toBigDecimal())


/**
 * Indexing operator overload for setting [PreparedStatement] bind variables.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given value at.
 *
 * @param value Value to set at the given index.
 */
operator fun <T> PreparedStatement.set(index: Int, value: T) =
  setObject(index, value)

/**
 * Indexing operator overload for setting [PreparedStatement] bind variables.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given value at.
 *
 * @param type SQL type of the given value.
 *
 * @param value Value to set at the given index.
 */
operator fun <T> PreparedStatement.set(index: Int, type: SQLType, value: T) =
  setObject(index, value, type)

/**
 * Indexing operator overload for setting [PreparedStatement] bind variables.
 *
 * @receiver PreparedStatement instance to mutate.
 *
 * @param index Index to set the given value at.
 *
 * @param type SQL type of the given value.
 *
 * @param value Value to set at the given index.
 */
operator fun <T> PreparedStatement.set(index: Int, type: Int, value: T) =
  setObject(index, value, type)
