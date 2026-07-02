package com.androiddrop.service.discovery

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiscoveryServiceConnection : ServiceConnection {

    private val _boundService = MutableStateFlow<DiscoveryService?>(null)
    val boundService: StateFlow<DiscoveryService?> = _boundService.asStateFlow()

    private val _isBound = MutableStateFlow(false)
    val isBound: StateFlow<Boolean> = _isBound.asStateFlow()

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as DiscoveryService.DiscoveryBinder
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
