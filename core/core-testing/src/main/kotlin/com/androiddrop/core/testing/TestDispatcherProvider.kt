package com.androiddrop.core.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Proveedor de dispatchers de test para corrutinas.
 *
 * POR QUÉ un objeto proveedor vs instancias directas: Centralizar la creación
 * de dispatchers de test permite:
 *   1. Configuración consistente (mismo [TestCoroutineScheduler] entre tests)
 *   2. Fácil mocking en tests que necesitan control fino del tiempo virtual
 *   3. Cambiar la implementación de dispatcher globalmente (ej: pasar de
 *      [UnconfinedTestDispatcher] a [StandardTestDispatcher] para tests de
 *      concurrencia)
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestDispatcherProvider {

    private val scheduler = TestCoroutineScheduler()

    /**
     * Retorna un [TestDispatcher] para usar en tests.
     *
     * Por defecto usa [UnconfinedTestDispatcher] que ejecuta las corrutinas
     * de manera eager (sin demora), ideal para tests simples. Para tests que
     * necesitan verificar temporización, usar [StandardTestDispatcher].
     *
     * @param scope Scope de corrutinas (opcional, para vincular el scheduler).
     * @return TestDispatcher configurado.
     */
    fun testDispatcher(
        scope: CoroutineScope? = null
    ): TestDispatcher = UnconfinedTestDispatcher(scheduler)
}
