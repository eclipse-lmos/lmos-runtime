/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel

import org.slf4j.LoggerFactory.getLogger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * A Result class that enables the type of error case to be defined.
 * In kotlin, exceptions are meant to be returned as opposed to being thrown.
 * The Result class helps to simplify that approach.
 *
 * (https://elizarov.medium.com/kotlin-and-exceptions-8062f589d07)
 */
sealed class Result<out T, out E : Exception>
data class Success<out T>(val value: T) : Result<T, Nothing>()
data class Failure<out E : Exception>(val reason: E) : Result<Nothing, E>()

/**
 * Returns the value produced by the [block] parameter as a [Result].
 * If an exception is thrown within the [block] that matches the type declared on the [Result],
 * then that exception is returned as the result.
 * All other exceptions will be rethrown.
 */
inline fun <R, reified E : Exception> Any.result(block: ResultBlock<E>.() -> R): Result<R, E> = try {
        Success(ResultBlock<E>().block())
    } catch (ex: Throwable) {
        if (ex is E) {
            Failure(ex)
        } else {
            getLogger(this::class.java).warn("Unexpected error encountered in ${this::class.simpleName}!...", ex)
            throw ex
        }
    }

/**
 * Type safe way of throwing an exception.
 */
fun <E : Exception> ResultBlock<E>.failWith(block: () -> E): Nothing = throw block()

/**
 * Return the value of the success case or throws the exception from the "onFailure" block.
 * This acts as a bridge to the standard [kotlin.Result].
 */
context(ResultBlock<T>)
inline infix fun <R, T : Exception> kotlin.Result<R>.failWith(block: (Exception) -> T): R = try {
        getOrThrow()
    } catch (ex: Exception) {
        throw block(ex)
    }

/**
 * Return the value of the success case or throws the exception from the "block" block.
 */
context(ResultBlock<T>)
inline infix fun <R, E : Exception, T : Exception> Result<R, E>.failWith(block: (E) -> T): R = when (this) {
        is Success -> value
        is Failure -> throw block(reason)
    }

/**
 * If the predicate evaluates to true, then throw the exception form the body.
 * This should usually be called in a "catching" block.
 */
inline fun <E : Exception> ResultBlock<E>.ensure(predicate: Boolean, block: () -> E) {
    if (predicate) {
        throw block()
    }
}

/**
 * If the predicate is null, then throw the exception form the body.
 * This should usually be called in a "result" block.
 */
@OptIn(ExperimentalContracts::class)
inline fun <E : Exception> ResultBlock<E>.ensureNotNull(predicate: Any?, block: () -> E) {
    contract {
        returns() implies (predicate != null)
    }
    if (predicate == null) {
        throw block()
    }
}

/**
 * Calls [block] if the result is a [Failure].
 */
inline infix fun <R, E : Exception> Result<R, E>.onFailure(block: (E) -> Unit): Result<R, E> {
    if (this is Failure) block(reason)
    return this
}

/**
 * Return the value of the success case or throws the exception.
 */
fun <R, E : Exception> Result<R, E>.getOrThrow(): R = when (this) {
        is Success -> value
        is Failure -> throw reason
    }

/**
 * Return the value of the success case or null.
 */
fun <R, E : Exception> Result<R, E>.getOrNull(): R? = when (this) {
        is Success -> value
        is Failure -> null
    }

/**
 * Creates a new Result with the exception mapped to a new type.
 */
inline fun <R, E : Exception, T : Exception> Result<R, E>.mapFailure(transform: (E) -> T): Result<R, T> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(reason))
    }

/**
 * Creates a new Result with the value mapped to a new type.
 */
inline fun <R, E : Exception, T> Result<R, E>.map(transform: (R) -> T): Result<T, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

/**
 * Used to enforce that certain functions can only be called with the "result" block.
 */
class ResultBlock<E>
