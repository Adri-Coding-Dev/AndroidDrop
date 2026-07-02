package com.androiddrop.data.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.resume

class BleGattClient @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val GATT_TIMEOUT_MS = 10000L
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var servicesDiscovered = false
    private val notificationCallbacks = ConcurrentHashMap<UUID, (ByteArray) -> Unit>()

    suspend fun connectToDevice(device: BluetoothDevice): BluetoothGatt? =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val callback = object : BluetoothGattCallback() {
                        override fun onConnectionStateChange(
                            gatt: BluetoothGatt?,
                            status: Int,
                            newState: Int
                        ) {
                            when (newState) {
                                BluetoothProfile.STATE_CONNECTED -> {
                                    Timber.d("GATT conectado a ${device.address}")
                                    bluetoothGatt = gatt
                                    gatt?.discoverServices()
                                }
                                BluetoothProfile.STATE_DISCONNECTED -> {
                                    Timber.d("GATT desconectado de ${device.address}")
                                    bluetoothGatt = null
                                    servicesDiscovered = false
                                }
                            }
                        }

                        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                servicesDiscovered = true
                                Timber.d("Servicios GATT descubiertos")
                                continuation.resume(gatt)
                            } else {
                                Timber.w("Fallo al descubrir servicios GATT: $status")
                                continuation.resume(null)
                            }
                        }

                        override fun onCharacteristicRead(
                            gatt: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic,
                            value: ByteArray,
                            status: Int
                        ) {
                            // Manejar lectura
                        }

                        override fun onCharacteristicChanged(
                            gatt: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic,
                            value: ByteArray
                        ) {
                            val callback = notificationCallbacks[characteristic.uuid]
                            callback?.invoke(value)
                        }
                    }

                    device.connectGatt(context, false, callback)
                } catch (e: Exception) {
                    Timber.e(e, "Error al conectar GATT con ${device.address}")
                    continuation.resume(null)
                }
            }
        }

    fun discoverServices(): List<BluetoothGattService> {
        return bluetoothGatt?.services ?: emptyList()
    }

    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        return try {
            characteristic.value = value
            bluetoothGatt?.writeCharacteristic(characteristic) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error al escribir característica")
            false
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): ByteArray? {
        return try {
            bluetoothGatt?.readCharacteristic(characteristic)
            characteristic.value
        } catch (e: Exception) {
            Timber.e(e, "Error al leer característica")
            null
        }
    }

    fun enableNotifications(characteristic: BluetoothGattCharacteristic): Boolean {
        return try {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            ) ?: return false

            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.setCharacteristicNotification(characteristic, true) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error al habilitar notificaciones")
            false
        }
    }

    fun setNotificationCallback(uuid: UUID, callback: (ByteArray) -> Unit) {
        notificationCallbacks[uuid] = callback
    }

    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            servicesDiscovered = false
            notificationCallbacks.clear()
            Timber.d("GATT cliente desconectado")
        } catch (e: Exception) {
            Timber.e(e, "Error al desconectar GATT cliente")
        }
    }
}
