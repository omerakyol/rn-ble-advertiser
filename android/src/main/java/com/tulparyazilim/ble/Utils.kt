package com.tulparyazilim.ble

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter 
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat  
import android.os.ParcelUuid
import java.util.*

object Utils {

	private val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
	private val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
	private val CHANNEL_NAME = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME
	private val TAG = "Utils"
	
	/* Bluetooth API */
	private var bluetoothManager: BluetoothManager? = null
	private var bluetoothGattServer: BluetoothGattServer? = null

	fun startBluetoothMonitoringService(context: Context) {
		startServer(context)
		startAdvertising()
	}
   
	fun stopBluetoothMonitoringService() {
		stopServer()
		stopAdvertising()
		Log.i(TAG, "stopBluetoothMonitoringService: Stopping ble service")
	}

	/**
	 * Callback to receive information about the advertisement process.
	 */
	private val advertiseCallback = object : AdvertiseCallback() {
		override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
			Log.i(TAG, "LE Advertise Started.")
		}

		override fun onStartFailure(errorCode: Int) {
			Log.w(TAG, "LE Advertise Failed: $errorCode")
		}
	}


	/**
	 * Callback to handle incoming requests to the GATT server.
	 * All read/write requests for characteristics and descriptors are handled here.
	 */
	private val gattServerCallback = object : BluetoothGattServerCallback() {

		override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "BluetoothDevice CONNECTED: $device")
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "BluetoothDevice DISCONNECTED: $device")
			}
		}

		override fun onCharacteristicReadRequest(
			device: BluetoothDevice, requestId: Int, offset: Int,
			characteristic: BluetoothGattCharacteristic
		) {
			try {
				when (characteristic.uuid) {
					UUID.fromString(BuildConfig.V1_CHARACTERISTIC_ID) -> {
						Log.i(TAG, "onCharacteristicReadRequest: Reading data")
						val base = AppPreferences.userId?.toByteArray() ?: "na".toByteArray()
						Log.i(TAG, "AppPreferences userId: $AppPreferences.userId")
						val value = base.copyOfRange(offset, base.size)
						Log.i(TAG, "onCharacteristicReadRequest value: $value")

						bluetoothGattServer?.sendResponse(
							device,
							requestId,
							BluetoothGatt.GATT_SUCCESS,
							0,
							value
						)
					}
					else -> {
						// Invalid characteristic
						Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
						bluetoothGattServer?.sendResponse(
							device,
							requestId,
							BluetoothGatt.GATT_FAILURE,
							0,
							null
						)
					}
				}
			} catch (e: Exception) { 
				e.printStackTrace()

				Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
				bluetoothGattServer?.sendResponse(
						device,
						requestId,
						BluetoothGatt.GATT_FAILURE,
						0,
						null
				)

			}
		}
	}
  

	/**
	 * Begin advertising over Bluetooth that this device is connectable
	 * and supports the Current Time Service.
	 */
	private fun startAdvertising() {
		val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
				bluetoothManager?.adapter?.bluetoothLeAdvertiser

		bluetoothLeAdvertiser?.let {
			val settings = AdvertiseSettings.Builder()
					.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
					.setConnectable(true)
					.setTimeout(0)
					.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
					.build()

			val data = AdvertiseData.Builder()
					.setIncludeDeviceName(false)
					.setIncludeTxPowerLevel(false)
					.addServiceUuid(
							ParcelUuid(UUID.fromString(BuildConfig.SERVICE_ID)),
					)
					.build()

			it.startAdvertising(settings, data, advertiseCallback)
			Log.w(TAG, "BluetoothLeAdvertiser started advertising")

		} ?: Log.w(TAG, "BluetoothLeAdvertiser failed to create advertising")
	}

	/**
	 * Stop Bluetooth advertisements.
	 */
	private fun stopAdvertising() {
		val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
				bluetoothManager?.adapter?.bluetoothLeAdvertiser
		bluetoothLeAdvertiser?.let {
			it.stopAdvertising(advertiseCallback)
			Log.w(TAG, "BluetoothLeAdvertiser stopped advertising")
		} ?: Log.w(TAG, "BluetoothLeAdvertiser failed to stop advertising")
	}

	/**
	 * Initialize the GATT server instance with the services/characteristics
	 * from the Time Profile.
	 */
	private fun startServer(context: Context) {
		if (bluetoothManager == null) {
			bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
			Log.w(TAG, "Bluetooth Manager is initialized")
		}
		if (bluetoothGattServer == null) {
			bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
			Log.w(TAG, "Bluetooth Gatt Server is initialized")
		}
		bluetoothGattServer?.addService(createCustomService())
		Log.w(TAG, "Bluetooth Gatt Server Service is added")
	}

	/**
	 * Shut down the GATT server.
	 */
	private fun stopServer() {
		bluetoothGattServer?.close()
		bluetoothGattServer = null
	}

	private fun createCustomService(): BluetoothGattService {
		val gattService = BluetoothGattService(
			UUID.fromString(BuildConfig.SERVICE_ID),
			BluetoothGattService.SERVICE_TYPE_PRIMARY
		)

		val characteristicV2 = BluetoothGattCharacteristic(
			UUID.fromString(BuildConfig.V1_CHARACTERISTIC_ID),
			BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
			BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
		)

		gattService.addCharacteristic(characteristicV2)
		return gattService
	}

}
