package com.tulparyazilim.ble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log 

class RestartReceiver : BroadcastReceiver() {

	companion object {
		private const val TAG = "RestartReceiver"
	}

	override fun onReceive(context: Context, intent: Intent) {
		try {
			if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
				Log.d(TAG, "onReceive: Starting BLE monitoring service")
				Utils.startBluetoothMonitoringService(context)
			}
		} catch (e: Exception) { 
			e.printStackTrace()
		}
	}
}
