package com.androiddrop.service.discovery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "android_drop_discovery"
        const val NOTIFICATION_ID = 1001
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Descubrimiento de dispositivos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificación para el servicio de descubrimiento BLE"
            setSound(null, null)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(deviceCount: Int): Notification {
        val contentText = if (deviceCount > 0) {
            "$deviceCount dispositivo${if (deviceCount != 1) "s" else ""} encontrado${if (deviceCount != 1) "s" else ""}"
        } else {
            "Buscando dispositivos cercanos..."
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("AndroidDrop")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
