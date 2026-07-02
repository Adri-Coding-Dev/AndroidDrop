package com.androiddrop.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Proveedor de dispatchers mockeables para tests.
 *
 * POR QUÉ dispatchers mockeables: Muchos componentes de AndroidDrop reciben
 * [CoroutineDispatcher] via inyección de dependencias para poder controlar
 * en qué hilo ejecutan su trabajo. En tests, estos dispatchers deben ser
 * reemplazados por [TestDispatcher] para evitar bloqueos y permitir control
 * del tiempo virtual.
 *
 * Uso típico con MockK:
 * ```
 * every { mockDispatchers.io } returns TestDispatcherProvider.testDispatcher()
 * ```
 *
 * @property io Dispatcher para operaciones de I/O (red, disco).
 * @property default Dispatcher para operaciones intensivas (crypto, encoding).
 * @property main Dispatcher para UI y actualización de estado.
 */
data class MockDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val default: CoroutineDispatcher = Dispatchers.Default,
    val main: CoroutineDispatcher = Dispatchers.Main
) {
    companion object {
        /**
         * Crea una instancia con todos los dispatchers reemplazados por
         * [TestDispatcher] para tests unitarios.
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun createTestInstance(): MockDispatchers = MockDispatchers(
            io = UnconfinedTestDispatcher(),
            default = UnconfinedTestDispatcher(),
            main = UnconfinedTestDispatcher()
        )
    }
}
