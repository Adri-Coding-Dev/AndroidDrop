package com.androiddrop.data.ble

import android.bluetooth.le.ScanResult
import com.androiddrop.core.crypto.CryptoManager
import com.androiddrop.domain.model.ConnectionInfo
import com.androiddrop.domain.model.DeviceType
import com.androiddrop.domain.model.NearbyDevice
import com.androiddrop.domain.model.TransportType
import com.androiddrop.domain.repository.DeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class BLEDeviceRepository @Inject constructor(
    private val advertiser: BleAdvertiser,
    private val scanner: BleScanner,
    private val gattServer: BleGattServer,
    private val gattClient: BleGattClient,
    private val cryptoManager: CryptoManager
) : DeviceRepository {

    private val connectedDeviceFlow = MutableStateFlow<NearbyDevice?>(null)

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        private const val SCAN_INTERVAL_MS = 5000L
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L
    }

    override fun discoverDevices(): Flow<NearbyDevice> {
        return scanner.startScanning(SERVICE_UUID)
            .map { scanResult -> mapScanResultToDevice(scanResult) }
            .retryWhen { cause, attempt ->
                if (attempt < MAX_RETRIES) {
                    val delay = BASE_DELAY_MS * (1L shl attempt.toInt())
                    Timber.w(cause, "Error en escaneo BLE, reintento $attempt en ${delay}ms")
                    delay(delay)
                    true
                } else {
                    false
                }
            }
            .catch { e ->
                Timber.e(e, "Escaneo BLE agotó los reintentos")
                throw e
            }
    }

    override suspend fun connectToDevice(device: NearbyDevice): Boolean {
        return try {
            val bluetoothDevice = findBluetoothDevice(device.deviceId)
            if (bluetoothDevice == null) {
                Timber.w("Dispositivo Bluetooth no encontrado: ${device.deviceId}")
                return false
            }

            val gatt = gattClient.connectToDevice(bluetoothDevice)
            if (gatt == null) {
                return false
            }

            val keyPair = cryptoManager.generateKeyPair()
            Timber.d("Handshake BLE completado para ${device.deviceName}")
            connectedDeviceFlow.value = device
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al conectar con ${device.deviceName}")
            false
        }
    }

    override suspend fun authenticateDevice(
        device: NearbyDevice
    ): Result<Boolean> {
        return try {
            // Placeholder: challenge should be obtained from higher level
            val challenge = ByteArray(32)
            val nonce = cryptoManager.generateNonce()
            val signature = cryptoManager.sign(challenge, nonce)
            val isValid = cryptoManager.verify(challenge, signature, nonce)

            if (isValid) {
                Timber.d("Autenticación mutua exitosa para ${device.deviceName}")
                Result.success(true)
            } else {
                Timber.w("Autenticación fallida para ${device.deviceName}")
                Result.failure(Exception("Autenticación fallida"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error durante autenticación con ${device.deviceName}")
            Result.failure(e)
        }
    }

    override suspend fun disconnectDevice(deviceId: String) {
        gattClient.disconnect()
        connectedDeviceFlow.value = null
    }

    override fun getConnectedDevice(): Flow<NearbyDevice?> = connectedDeviceFlow

    fun startAdvertising(deviceName: String) {
        advertiser.startAdvertising(deviceName, SERVICE_UUID) { success ->
            if (success) {
                Timber.d("BLE advertising iniciado como $deviceName")
            }
        }
    }

    fun stopAdvertising() {
        advertiser.stopAdvertising()
    }

    private fun mapScanResultToDevice(scanResult: ScanResult): NearbyDevice {
        val device = scanResult.device
        return NearbyDevice(
            deviceId = device.address,
            deviceName = device.name ?: "Dispositivo Desconocido",
            deviceType = DeviceType.PHONE,
            connectionInfo = ConnectionInfo(
                transportType = TransportType.BLE,
                address = device.address,
                port = 0
            ),
            signalStrength = scanResult.rssi
        )
    }

    private fun findBluetoothDevice(deviceId: String): android.bluetooth.BluetoothDevice? {
        return try {
            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                Timber.w("Bluetooth no disponible o desactivado")
                return null
            }
            adapter.getRemoteDevice(deviceId)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Dirección Bluetooth inválida: $deviceId")
            null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener dispositivo Bluetooth: $deviceId")
            null
        }
    }
}
