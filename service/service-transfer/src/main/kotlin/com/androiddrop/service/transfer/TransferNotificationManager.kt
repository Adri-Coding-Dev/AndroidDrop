package com.androiddrop.service.transfer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "android_drop_transfer"
        const val NOTIFICATION_ID = 2001
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transferencia de archivos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificación para el servicio de transferencia de archivos"
            setSound(null, null)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildProgressNotification(
        sessionId: String,
        progress: Float,
        speedBps: Long
    ): Notification {
        val progressInt = (progress * 100).toInt()
        val speedText = formatearVelocidad(speedBps)
        val cancelIntent = crearCancelPendingIntent(sessionId)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AndroidDrop")
            .setContentText("Transfiriendo archivos... $speedText")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progressInt, false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancelar",
                cancelIntent
            )
            .build()
    }

    fun buildCompleteNotification(sessionId: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AndroidDrop")
            .setContentText("Transferencia completada")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun buildFailedNotification(sessionId: String, error: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AndroidDrop")
            .setContentText("Transferencia fallida: $error")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun crearCancelPendingIntent(sessionId: String): PendingIntent {
        val intent = Intent(context, TransferBroadcastReceiver::class.java).apply {
            action = TransferBroadcastReceiver.ACTION_CANCEL_TRANSFER
            putExtra(TransferBroadcastReceiver.EXTRA_SESSION_ID, sessionId)
        }
        return PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatearVelocidad(bps: Long): String {
        return when {
            bps >= 1_000_000 -> "${bps / 1_000_000} MB/s"
            bps >= 1_000 -> "${bps / 1_000} KB/s"
            else -> "$bps B/s"
        }
    }
}
