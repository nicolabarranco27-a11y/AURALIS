package com.auralis.player.core.common.result

/** Resultado tipado para operaciones que pueden fallar. */
sealed interface AppResult<out T> {

    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.value

    companion object {
        inline fun <T> of(block: () -> T): AppResult<T> = try {
            Success(block())
        } catch (t: Throwable) {
            if (t is kotlinx.coroutines.CancellationException) throw t
            Failure(AppError.Unexpected(t.message ?: "Error inesperado", t))
        }
    }
}

sealed class AppError(open val message: String) {

    data class Unexpected(override val message: String, val cause: Throwable? = null) :
        AppError(message)

    data class NotFound(override val message: String) : AppError(message)
}
