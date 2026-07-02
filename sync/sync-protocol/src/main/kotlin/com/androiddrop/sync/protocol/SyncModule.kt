package com.androiddrop.sync.protocol

import com.androiddrop.core.network.SocketManager
import com.androiddrop.domain.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee las dependencias del subsistema de sincronización.
 *
 * POR QUÉ Singleton para SyncProtocol: La sincronización se mantiene durante
 * toda la sesión de transferencia. Un solo SyncProtocolImpl maneja el flujo
 * de frames durante la sesión activa. Crear uno nuevo para cada frame sería
 * ineficiente y rompería la continuidad de la secuencia de frames.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    /**
     * Provee la implementación por defecto de [SyncProtocol].
     *
     * @param socketManager Gestor de sockets TCP para comunicación.
     * @return SyncProtocolImpl listo para sincronizar.
     */
    @Provides
    @Singleton
    fun provideSyncProtocol(
        socketManager: SocketManager
    ): SyncProtocol {
        return SyncProtocolImpl(socketManager)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(impl: SyncRepositoryImpl): SyncRepository {
        return impl
    }

    /**
     * Provee el sincronizador de clock.
     *
     * @return ClockSynchronizer nuevo (no singleton porque es por sesión).
     */
    @Provides
    fun provideClockSynchronizer(): ClockSynchronizer {
        return ClockSynchronizer()
    }

    /**
     * Provee el motor de interpolación de frames.
     *
     * @return InterpolationEngine nuevo (no singleton porque es por sesión).
     */
    @Provides
    fun provideInterpolationEngine(): InterpolationEngine {
        return InterpolationEngine()
    }
}
