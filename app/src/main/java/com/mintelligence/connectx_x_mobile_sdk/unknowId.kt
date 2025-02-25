package com.mintelligence.connectx_x_mobile_sdk

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mintelligence.connectxmobilesdk.ConnectXManager
import kotlinx.coroutines.launch

class UnknownId(private val lifecycleOwner: LifecycleOwner, private val unknownIdText: (String) -> Unit) {

    fun getUnknownIdFromServer() {
        lifecycleOwner.lifecycleScope.launch {
            ConnectXManager.cxTracking(mapOf(
                "cx_event" to "get unknownId kotlin",
            ))
            try {
                // Simulating the fetch of Unknown ID (replace with actual ConnectXManager call)
                val unknownId = ConnectXManager.getUnknownId() // Fetch from the server

                // Update the UI with the fetched unknown ID
                unknownIdText(unknownId ?: "Failed to get Unknown ID")
            } catch (e: Exception) {
                e.printStackTrace()
                unknownIdText("Error fetching Unknown ID")
            }
        }
    }
}
