package com.androiddrop.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var foundDevices = mutableSetOf<String>()

    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
            return bluetoothManager?.adapter
        }

    fun startScanning(serviceUuid: UUID): Flow<ScanResult> = callbackFlow {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            Timber.w("BLE no disponible o desactivado")
            close()
            return@callbackFlow
        }

        bluetoothLeScanner = adapter.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Timber.w("BluetoothLeScanner no soportado")
            close()
            return@callbackFlow
        }

        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(serviceUuid))
                .build()
        )

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val deviceAddress = result.device?.address ?: return
                foundDevices.add(deviceAddress)
                trySend(result)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                for (result in results) {
                    val deviceAddress = result.device?.address ?: continue
                    foundDevices.add(deviceAddress)
                    trySend(result)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Timber.w("BLE scan falló con código: $errorCode")
            }
        }

        isScanning = true
        bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)

        awaitClose {
            isScanning = false
            bluetoothLeScanner?.stopScan(scanCallback)
            foundDevices.clear()
            Timber.d("BLE scanning detenido")
        }
    }

    fun stopScanning() {
        isScanning = false
        bluetoothLeScanner?.let { scanner ->
            // El callback específico se detiene al cerrar el Flow
        }
        foundDevices.clear()
    }
}
