package com.tulparyazilim.ble

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod 


class ReactNativeBleAdvertiserModule(reactContext: ReactApplicationContext) :
	ReactContextBaseJavaModule(reactContext) {

	companion object {
		private const val TAG = "RNBleAdvertiserModule"
	}

	init {
		AppPreferences.init(reactContext.applicationContext)
	}

	override fun getName(): String {
		return "ReactNativeBleAdvertiser"
	}

	@ReactMethod
	fun startBroadcast(data: String) {
		try {

			AppPreferences.userId = data
			Log.i(TAG, "setData $data in App prefs as '${AppPreferences.userId}'")

			AppPreferences.needStart = true
			Log.i(TAG, "Start Service")
			Utils.startBluetoothMonitoringService(reactApplicationContext)

		} catch (e: Exception) {
			e.printStackTrace()
			Log.i(TAG, "startBroadcast error: " + e.printStackTrace())
		}
	}

	@ReactMethod
	fun stopBroadcast() {
		try {
			AppPreferences.userId = ""
			AppPreferences.needStart = false
			Utils.stopBluetoothMonitoringService()
			Log.i(TAG, "stopBroadcast")
		} catch (e: Exception) {
			e.printStackTrace()
			Log.i(TAG, "stopBroadcast error: " + e.printStackTrace())
		}
	}
}
