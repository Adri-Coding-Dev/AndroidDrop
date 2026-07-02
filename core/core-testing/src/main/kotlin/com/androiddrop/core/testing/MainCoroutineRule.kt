package com.androiddrop.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla de JUnit que reemplaza [Dispatchers.Main] con un [TestDispatcher].
 *
 * POR QUÉ esta regla: En tests de Android, [Dispatchers.Main] está vinculado
 * al looper del hilo principal, que no está disponible en tests unitarios
 * (solo en tests de instrumentación). Esta regla reemplaza el dispatcher
 * Main por uno de test, permitiendo que las corrutinas se ejecuten de manera
 * determinista y controlada en tests unitarios.
 *
 * Uso:
 * ```
 * @get:Rule
 * val mainCoroutineRule = MainCoroutineRule()
 * ```
 *
 * @property testDispatcher Dispatcher usado para las pruebas (por defecto
 *   [UnconfinedTestDispatcher] que ejecuta las corrutinas inmediatamente).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
