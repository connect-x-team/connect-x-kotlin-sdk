package com.mintelligence.connectx_x_mobile_sdk

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mintelligence.connectx_x_mobile_sdk.ui.theme.Connectx_x_mobile_sdkTheme
import com.mintelligence.connectxmobilesdk.ConnectXManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Run the initialization on a background thread
                    ConnectXManager.initialize(
                        applicationContext, "YOUR_TOKEN", "YOUR_ORG_ID")

                    val trackingData = mapOf(
                        "cx_event" to "open app kotlin",
                    )
                    ConnectXManager.cxTracking(trackingData)
                }
                // If successful, update the UI on the main thread
                setContent {
                    Connectx_x_mobile_sdkTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppContent()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                setContent {
                    Connectx_x_mobile_sdkTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
//                            Greeting("Initialization Failed: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val trackingData = mapOf(
            "cx_event" to "app pause kotlin",
        )
        ConnectXManager.cxTracking(trackingData)
    }

    override fun onResume() {
        super.onResume()
        val trackingData = mapOf(
            "cx_event" to "app resume kotlin",
        )
        ConnectXManager.cxTracking(trackingData)
    }
}

@Composable
fun AppContent() {
    // Declare your state variables here inside Composable function
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var isFormVisible by remember { mutableStateOf(false) }
    var isTicketVisible by remember { mutableStateOf(false) }
    var isRecordVisible by remember { mutableStateOf(false) }

    var ticketName by remember { mutableStateOf(TextFieldValue("")) }
    var connectorEmail by remember { mutableStateOf(TextFieldValue("")) }
    var customerEmail by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf(TextFieldValue("")) }

    var objectName by remember { mutableStateOf(TextFieldValue("")) }
    var cxName by remember { mutableStateOf(TextFieldValue("")) }
    var docId by remember { mutableStateOf(TextFieldValue("")) }

    var unknownIdText by remember { mutableStateOf("Unknown ID not fetched yet") }

    // Assuming you have access to a LifecycleOwner (e.g., activity or fragment)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize UnknownId with the lifecycleOwner and UI update function
    val unknownIdFetcher = remember { UnknownId(lifecycleOwner) { updatedText -> unknownIdText = updatedText } }

    Box(
        modifier = Modifier
            .fillMaxSize() // Makes the Box fill the entire screen
            .padding(16.dp), // Optional padding for the entire Box
        contentAlignment = Alignment.Center // Centers the content inside the Box
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                isFormVisible = !isFormVisible
                val trackingData = mapOf(
                    "cx_event" to if (isFormVisible) "open form kotlin" else "close form kotlin",
                    "cx_source" to "custom source from kotlin",
                    "cx_type" to "custom type from kotlin"
                )
                ConnectXManager.cxTracking(trackingData)
            }) {
                Text("Show Form")
            }

            // Conditionally show the form
            if (isFormVisible) {
//                val trackingData = mapOf(
//                    "cx_event" to "show form kotlin",
//                )
//                ConnectXManager.cxTracking(trackingData)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Enter Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger the action when form is submitted
                Button(onClick = {
                    val customerData = mapOf(
                        "cx_userId" to "", // Assuming email is being used as cx_userId
                        "cx_Name" to name.text,
                        "cx_firstName" to name.text,
                        "cx_lastName" to name.text,
                    )

                    val tracking = mapOf(
                        "cx_title" to "kotlin tracking",
                        "cx_type" to "Android Mobile",
                    )
                    val eventData = mapOf(
                        "cx_timeStamp" to "2025-01-01T00:00:00Z"
                    )
                    val options = mapOf(
                        "updateCustomer" to false
                    )

                    // Call cxIdentify method
                    ConnectXManager.cxIdentify(
                        mapOf("key" to "cx_Name", "customers" to customerData, "tracking" to tracking)
                    )
                    println("Name: ${name.text}, Email: ${email.text}")
                    isFormVisible = false
                    ConnectXManager.cxTracking(mapOf(
                        "cx_event" to "submit form kotlin",
                    ))
                }) {
                    Text("Submit")
                }
            }

            Button(onClick = {
                isTicketVisible = !isTicketVisible
                ConnectXManager.cxTracking(mapOf(
                    "cx_event" to if (isTicketVisible) "open ticket kotlin" else "close ticket kotlin",
                ))
            }) {
                Text("Open ticket")
            }

            if (isTicketVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = ticketName,
                    onValueChange = { ticketName = it },
                    label = { Text("Enter Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = connectorEmail,
                    onValueChange = { connectorEmail = it },
                    label = { Text("Enter Connector Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = customerEmail,
                    onValueChange = { customerEmail = it },
                    label = { Text("Enter Customer Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Enter Content") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger the action when form is submitted
                Button(onClick = {
                    // Call cxIdentify method
                    ConnectXManager.cxOpenTicket(
                        mapOf(
                            "key" to "cx_Name",
                            "customers" to mapOf(
                                "cx_Name" to ticketName.text,
                                "cx_firstName" to ticketName.text,
                                "cx_phone" to "0000000000",
                                "cx_mobilePhone" to "0000000000",
                                "cx_email" to customerEmail.text
                            ),
                            "ticket" to mapOf(
                                "cx_subject" to "test email",
                                "cx_socialAccountName" to connectorEmail.text, // e.g., manow.seem2@gmail.com
                                "cx_socialContact" to customerEmail.text,
                                "cx_channel" to "email",
                                "email" to mapOf(
                                    "text" to "from mobile app",
                                    "html" to "<b>${content.text}</b>"
                                )
                            ),
                            "lead" to mapOf(
                                "cx_email" to "xxxx@hotmail.com",
                                "cx_channel" to "test_connect_email"
                            ),
                            "customs" to listOf(
                                mapOf("customObjectA" to mapOf("cx_Name" to "Test")),
                                mapOf("customObjectB" to mapOf("cx_Name" to "Test"))
                            )
                        )
                    )

                    println("Name: ${ticketName.text}, Email: ${customerEmail.text}")
                    isTicketVisible = false
                    ConnectXManager.cxTracking(mapOf(
                        "cx_event" to "submit ticket kotlin",
                    ))
                }) {
                    Text("Submit")
                }
            }

            Button(onClick = {
                isRecordVisible = !isRecordVisible
                ConnectXManager.cxTracking(mapOf(
                    "cx_event" to if (isRecordVisible) "open create record kotlin" else "close create record kotlin",
                ))
            }) {
                Text("Create Record")
            }

            // Conditionally show the form
            if (isRecordVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = objectName,
                    onValueChange = { objectName = it },
                    label = { Text("Enter Object Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = cxName,
                    onValueChange = { cxName = it },
                    label = { Text("Enter cxName") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = docId,
                    onValueChange = { docId = it },
                    label = { Text("Enter DocId") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger the action when form is submitted
                Button(onClick = {
                    val attributes = mapOf("referenceId" to "www1")
                    val data = listOf(
                        mapOf(
                            "attributes" to attributes,
                            "cx_Name" to cxName.text, // Replace cxName with your actual variable
                            "docId" to docId.text // Replace docId with your actual variable
                        )
                    )

                    ConnectXManager.cxCreateRecord(objectName.text, data)

                    println("Name: ${objectName.text}, Email: ${cxName.text}")
                    isRecordVisible = false
                    ConnectXManager.cxTracking(mapOf(
                        "cx_event" to "submit create record kotlin",
                    ))
                }) {
                    Text("Submit")
                }
            }

            Button(onClick = {
                unknownIdFetcher.getUnknownIdFromServer()
                ConnectXManager.cxTracking(mapOf(
                    "cx_event" to "get unknown id kotlin",
                ))
            }) {
                Text("Fetch Unknown ID")
            }

            // Display the unknown ID

            Text(unknownIdText)
        }
    }
}