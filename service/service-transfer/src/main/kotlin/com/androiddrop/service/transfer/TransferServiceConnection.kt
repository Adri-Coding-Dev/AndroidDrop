package com.androiddrop.service.transfer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransferServiceConnection : ServiceConnection {

    private val _boundService = MutableStateFlow<TransferService?>(null)
    val boundService: StateFlow<TransferService?> = _boundService.asStateFlow()

    private val _isBound = MutableStateFlow(false)
    val isBound: StateFlow<Boolean> = _isBound.asStateFlow()

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as TransferService.TransferBinder
        _boundService.value = binder.getService()
        _isBound.value = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
        _boundService.value = null
        _isBound.value = false
    }

    fun unbind() {
        _boundService.value = null
        _isBound.value = false
    }
}
