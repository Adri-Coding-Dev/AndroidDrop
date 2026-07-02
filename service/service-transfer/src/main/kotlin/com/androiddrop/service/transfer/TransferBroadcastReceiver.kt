package com.androiddrop.service.transfer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class TransferBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL_TRANSFER = "com.androiddrop.action.CANCEL_TRANSFER"
        const val EXTRA_SESSION_ID = "extra_session_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CANCEL_TRANSFER) {
            val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
            Timber.d("TransferBroadcastReceiver: cancelar transferencia $sessionId")

            val serviceIntent = Intent(context, TransferService::class.java).apply {
                action = ACTION_CANCEL_TRANSFER
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            context.startService(serviceIntent)
        }
    }
}
