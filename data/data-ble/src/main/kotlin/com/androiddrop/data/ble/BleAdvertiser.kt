package com.androiddrop.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class BleAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var currentCallback: AdvertiseCallback? = null

    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            return bluetoothManager?.adapter
        }

    fun startAdvertising(
        localDeviceName: String,
        serviceUuid: UUID,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val adapter = bluetoothAdapter
            if (adapter == null || !adapter.isEnabled) {
                Timber.w("BLE no disponible o desactivado")
                onResult(false)
                return
            }

            bluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser
            if (bluetoothLeAdvertiser == null) {
                Timber.w("BluetoothLeAdvertiser no soportado")
                onResult(false)
                return
            }

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            val advertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(android.os.ParcelUuid(serviceUuid))
                .build()

            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Timber.d("BLE advertising iniciado exitosamente")
                    onResult(true)
                }

                override fun onStartFailure(errorCode: Int) {
                    Timber.w("BLE advertising falló con código: $errorCode")
                    onResult(false)
                }
            }

            currentCallback = callback
            bluetoothLeAdvertiser?.startAdvertising(settings, advertiseData, callback)
        } catch (e: Exception) {
            Timber.e(e, "Error al iniciar BLE advertising")
            onResult(false)
        }
    }

    fun stopAdvertising() {
        try {
            currentCallback?.let { callback ->
                bluetoothLeAdvertiser?.stopAdvertising(callback)
                currentCallback = null
            }
            Timber.d("BLE advertising detenido")
        } catch (e: Exception) {
            Timber.e(e, "Error al detener BLE advertising")
        }
    }
}
