package dev.dotworld.ble

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
	fun initializeBle() {
		Log.i(TAG, "initialize: initializeBle Called")
	}

	@ReactMethod
	fun setData(data: String) {
		AppPreferences.userId = data
		Log.i(TAG, "setData ${AppPreferences.userId}")
	}

	@ReactMethod
	fun startBroadcast() {
		Log.i(TAG, "Start Service")
		try {
			if (AppPreferences.userId != null) {
				Utils.startBluetoothMonitoringService(reactApplicationContext)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@ReactMethod
	fun stopBroadcast() {
		Log.i(TAG, "stopBroadcast")
		try {
			Utils.stopBluetoothMonitoringService(reactApplicationContext)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
