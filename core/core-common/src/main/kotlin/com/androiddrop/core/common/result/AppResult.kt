package com.androiddrop.core.common.result

/**
 * Modelo de resultado para operaciones que pueden fallar.
 *
 * POR QUÉ AppResult vs excepciones: Las excepciones en Kotlin rompen el flujo
 * del programa y no son tipadas (no sabemos qué puede lanzar una función).
 * AppResult<out T> es un tipo sellado que fuerza al llamante a manejar ambos
 * casos (éxito y error) de manera explícita, haciendo el código más seguro
 * y predecible. Es una alternativa ligera a Either de Arrow que no requiere
 * dependencias externas.
 *
 * POR QUÉ out T (covarianza): Garantiza que AppResult<Subtipo> es subtipo de
 * AppResult<Tipo>, permitiendo polimorfismo en los resultados.
 *
 * @param T El tipo del valor en caso de éxito.
 */
sealed class AppResult<out T> {

    /**
     * Representa una operación exitosa.
     *
     * @param data El valor producido por la operación.
     */
    data class Success<T>(val data: T) : AppResult<T>()

    /**
     * Representa una operación fallida.
     *
     * @param exception La excepción técnica que causó el fallo (para logging).
     * @param message Mensaje legible para el usuario sobre el error.
     */
    data class Error(val exception: Throwable, val message: String) : AppResult<Nothing>()
}

/**
 * Retorna el valor si es [AppResult.Success], o null si es [AppResult.Error].
 *
 * POR QUÉ getOrNull: Útil en contextos donde el error no es crítico y se
 * prefiere un valor nulo como señal de fallo silencioso (ej: precarga de datos).
 */
fun <T> AppResult<T>.getOrNull(): T? = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> null
}

/**
 * Retorna el valor si es [AppResult.Success], o [default] si es [AppResult.Error].
 *
 * POR QUÉ getOrDefault: Similar a getOrNull pero permite proporcionar un
 * valor por defecto significativo (ej: lista vacía en lugar de null, 0 en lugar de null).
 */
fun <T> AppResult<T>.getOrDefault(default: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> default
}
