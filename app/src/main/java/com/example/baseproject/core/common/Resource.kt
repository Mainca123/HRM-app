package com.example.baseproject.core.common

/**
 * A sealed class that encapsulates successful, loading, and error states for network and database operations.
 * Signature professional pattern for Android developers.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
