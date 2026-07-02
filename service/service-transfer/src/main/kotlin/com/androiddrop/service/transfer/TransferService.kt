package com.androiddrop.service.transfer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

sealed interface TransferServiceState {
    data object Idle : TransferServiceState
    data class Transferring(
        val sessionId: String,
        val progress: Float,
        val speedBps: Long
    ) : TransferServiceState

    data class Complete(val sessionId: String) : TransferServiceState
    data class Failed(val sessionId: String, val error: String) : TransferServiceState
}

@AndroidEntryPoint
class TransferService : Service() {

    @Inject lateinit var notificationManager: TransferNotificationManager

    private val binder = TransferBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<TransferServiceState>(TransferServiceState.Idle)
    val state: StateFlow<TransferServiceState> = _state.asStateFlow()

    private val transferencias = mutableMapOf<String, TransferServiceState.Transferring>()

    inner class TransferBinder : Binder() {
        fun getService(): TransferService = this@TransferService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Timber.d("TransferService: onCreate")
        notificationManager.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("TransferService: onStartCommand")
        when (intent?.action) {
            TransferBroadcastReceiver.ACTION_CANCEL_TRANSFER -> {
                val sessionId = intent.getStringExtra(TransferBroadcastReceiver.EXTRA_SESSION_ID)
                if (sessionId != null) {
                    cancelarTransferencia(sessionId)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("TransferService: onDestroy")
        serviceScope.cancel()
    }

    fun iniciarTransferencia(sessionId: String) {
        val notification = notificationManager.buildProgressNotification(
            sessionId = sessionId,
            progress = 0f,
            speedBps = 0L
        )
        startForeground(TransferNotificationManager.NOTIFICATION_ID, notification)

        val estado = TransferServiceState.Transferring(
            sessionId = sessionId,
            progress = 0f,
            speedBps = 0L
        )
        transferencias[sessionId] = estado
        _state.value = estado
        Timber.d("TransferService: transferencia iniciada $sessionId")
    }

    fun actualizarProgreso(sessionId: String, progress: Float, speedBps: Long) {
        val estado = TransferServiceState.Transferring(
            sessionId = sessionId,
            progress = progress,
            speedBps = speedBps
        )
        transferencias[sessionId] = estado
        _state.value = estado

        val notification = notificationManager.buildProgressNotification(
            sessionId = sessionId,
            progress = progress,
            speedBps = speedBps
        )
        val notificationManagerCompat =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManagerCompat.notify(
            TransferNotificationManager.NOTIFICATION_ID,
            notification
        )
    }

    fun completarTransferencia(sessionId: String) {
        transferencias.remove(sessionId)
        _state.value = TransferServiceState.Complete(sessionId)

        val notification = notificationManager.buildCompleteNotification(sessionId)
        val notificationManagerCompat =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManagerCompat.notify(
            TransferNotificationManager.NOTIFICATION_ID,
            notification
        )

        if (transferencias.isEmpty()) {
            detenerServicio()
        }
    }

    fun fallarTransferencia(sessionId: String, error: String) {
        transferencias.remove(sessionId)
        _state.value = TransferServiceState.Failed(sessionId, error)

        val notification = notificationManager.buildFailedNotification(sessionId, error)
        val notificationManagerCompat =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManagerCompat.notify(
            TransferNotificationManager.NOTIFICATION_ID,
            notification
        )

        if (transferencias.isEmpty()) {
            detenerServicio()
        }
    }

    fun cancelarTransferencia(sessionId: String) {
        Timber.d("TransferService: cancelando transferencia $sessionId")
        transferencias.remove(sessionId)
        _state.value = TransferServiceState.Idle

        if (transferencias.isEmpty()) {
            detenerServicio()
        }
    }

    private fun detenerServicio() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
