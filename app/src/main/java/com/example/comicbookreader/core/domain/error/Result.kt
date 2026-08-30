package com.example.comicbookreader.core.domain.error

sealed interface Result<out D, out E : com.example.comicbookreader.core.domain.error.Error> {
    data class Success<out D, out E : com.example.comicbookreader.core.domain.error.Error>(
        val data: D
    ) : Result<D, E>

    data class Error<out D, out E : com.example.comicbookreader.core.domain.error.Error>(
        val error: E
    ) : Result<D, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline fun <D, E : Error, R> Result<D, E>.map(map: (D) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(map(data))
        is Result.Error -> Result.Error(error)
    }
}

fun <D, E : Error> Result<D, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}

inline fun <D, E : Error> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : Error> Result<D, E>.onFailure(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}