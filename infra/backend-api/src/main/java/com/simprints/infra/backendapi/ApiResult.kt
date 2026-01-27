package com.simprints.infra.backendapi

/**
 * Represents the result of an API call, encapsulating either a success data or a failure with a cause.
 */
sealed class ApiResult<T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>()

    data class Failure<T>(
        val cause: Throwable,
    ) : ApiResult<T>()

    /**
     * Returns the data if successful, or throws the failure cause if failed.
     * This is identical to the old behaviour only more explicit about throwing the exceptions.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw cause
    }

    /**
     * Returns the data if successful, or maps the failure into a valid result instance.
     */
    fun getOrMapFailure(block: (Failure<T>) -> T): T = when (this) {
        is Success -> data
        is Failure -> block(this)
    }
}
