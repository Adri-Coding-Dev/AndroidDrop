package com.androiddrop.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class BleGattServer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val HAND_SHAKE_SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val SYNC_SERVICE_UUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val HANDSHAKE_CHAR_UUID: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
        val SYNC_CHAR_UUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private var gattServer: BluetoothGattServer? = null
    private val connectedDevices = ConcurrentHashMap<String, BluetoothDevice>()
    private var serverCallback: BleGattServerCallback? = null

    private val bluetoothManager: BluetoothManager?
        get() = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    fun createGattServer(serviceUuid: UUID): BluetoothGattServer? {
        try {
            val manager = bluetoothManager ?: return null
            val adapter = manager.adapter ?: return null

            val callback = object : BluetoothGattServerCallback() {
                override fun onConnectionStateChange(
                    device: BluetoothDevice,
                    status: Int,
                    newState: Int
                ) {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            connectedDevices[device.address] = device
                            Timber.d("Dispositivo conectado: ${device.address}")
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            connectedDevices.remove(device.address)
                            Timber.d("Dispositivo desconectado: ${device.address}")
                        }
                    }
                    serverCallback?.onConnectionStateChange(device, status, newState)
                }

                override fun onCharacteristicReadRequest(
                    device: BluetoothDevice,
                    requestId: Int,
                    offset: Int,
                    characteristic: BluetoothGattCharacteristic
                ) {
                    serverCallback?.onCharacteristicReadRequest(
                        device, requestId, offset, characteristic
                    )?.let { response ->
                        gattServer?.sendResponse(
                            device, requestId,
                            BluetoothGatt.GATT_SUCCESS, offset, response
                        )
                    }
                }

                override fun onCharacteristicWriteRequest(
                    device: BluetoothDevice,
                    requestId: Int,
                    characteristic: BluetoothGattCharacteristic,
                    preparedWrite: Boolean,
                    responseNeeded: Boolean,
                    offset: Int,
                    value: ByteArray
                ) {
                    serverCallback?.onCharacteristicWriteRequest(
                        device, requestId, characteristic, value
                    ) ?: run {
                        if (responseNeeded) {
                            gattServer?.sendResponse(
                                device, requestId,
                                BluetoothGatt.GATT_SUCCESS, offset, value
                            )
                        }
                    }
                }

                override fun onDescriptorWriteRequest(
                    device: BluetoothDevice,
                    requestId: Int,
                    descriptor: BluetoothGattDescriptor,
                    preparedWrite: Boolean,
                    responseNeeded: Boolean,
                    offset: Int,
                    value: ByteArray
                ) {
                    gattServer?.sendResponse(
                        device, requestId,
                        BluetoothGatt.GATT_SUCCESS, offset, null
                    )
                }
            }

            val server = manager.openGattServer(context, callback)
            gattServer = server

            val handshakeService = BluetoothGattService(
                HAND_SHAKE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            val handshakeChar = BluetoothGattCharacteristic(
                HANDSHAKE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ or
                    BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            handshakeService.addCharacteristic(handshakeChar)

            val syncService = BluetoothGattService(
                SYNC_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            val syncChar = BluetoothGattCharacteristic(
                SYNC_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
            )
            syncService.addCharacteristic(syncChar)

            server?.addService(handshakeService)
            server?.addService(syncService)

            Timber.d("GATT Server creado con servicios de handshake y sincronización")
            return server
        } catch (e: Exception) {
            Timber.e(e, "Error al crear GATT server")
            return null
        }
    }

    fun notifyCharacteristicChanged(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        characteristic.value = value
        gattServer?.notifyCharacteristicChanged(device, characteristic, false)
    }

    fun setCallback(callback: BleGattServerCallback) {
        serverCallback = callback
    }

    fun close() {
        try {
            gattServer?.close()
            gattServer = null
            connectedDevices.clear()
            Timber.d("GATT Server cerrado")
        } catch (e: Exception) {
            Timber.e(e, "Error al cerrar GATT server")
        }
    }

    interface BleGattServerCallback {
        fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int)
        fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ): ByteArray?
        fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray?
        )
    }
}
