# ConnectX Mobile SDK

The **ConnectX Kotlin Mobile SDK** is a Android library designed to simplify the integration of ConnectX's tracking and data management features into your mobile applications. It provides functionalities for tracking events, managing user data, and interacting with backend APIs, ensuring seamless user analytics and experience tracking.

---

## Features

- **User Event Tracking**: Track user actions, behaviors, and interactions within your app.
- **Create Customer**: Identify and manage user details across sessions.
- **Open Ticket**: Open support tickets programmatically.
- **Create or Update Custom Records**: Create and edit records in bulk.
- **Get Unknown ID**: Generates and returns a unique identifier.

---

## Getting started

### Prerequisites
- A valid **ConnectX API Token** and **Organize ID** are required for initialization.

### Installation
Run the following command to add the package to your project:

```grandle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
Add it in your root settings.gradle at the end of repositories:


```grandle
dependencies {
   implementation 'tech.connect-x:connect-x-kotlin-sdk:x.x.x'
}
```
Add the dependency

## Usage

### 1. Import the Library

```kotlin
import com.connectx.connectxsdk.ConnectXManager
```


### 2. Initialize the SDK

Before using any SDK methods, initialize it with your token and organize ID.
Note: You can generate the YOUR_API_TOKEN from [Connect-X](https://app.connect-x.tech/) by navigating to:
Organization Settings → SDK Tracking.

```kotlin
ConnectXManager.initialize(
    applicationContext,
    "YOUR_TOKEN",
    "YOUR_ORGANIZE_ID",
    "YOUR_ENV" //optional
)
```

### 3. Track Events

```kotlin
val trackingData = mapOf(
    "cx_title" to "YOUR_EVENT_NAME",
    "cx_event" to "YOUR_EVENT",
    "cx_source" to "YOUR_SOURCE",
    // other Activity field
)

ConnectXManager.cxTracking(trackingData)
```

### 4. Create Customer

```kotlin

val payload = mapOf(
    "key" to "cx_Name",
    "customers" to mapOf(
        "cx_Name" to name.text,
        "cx_firstName" to name.text,
        "cx_lastName" to name.text,
        //... Other Customer Field
    ), 
    "tracking" to mapOf( // Optional
        "cx_title" to "YOUR_EVENT_NAME",
        "cx_event" to "YOUR_EVENT",
        // other Activity field
    ), 
    "form" to mapOf( // Optional
        "cx_subject" to "YOUR_SUBJECT",
        "cx_desc" to "YOUR_DESC"
        // ... Other Form Field.
    ), 
    "options" to mapOf( // Optional
        "updateCustomer" to true, // Enable/Disable Customer Data Update
        "customs" to listOf(
            // For adding values in the Object that you want to reference with the Customer Object.
            mapOf("customObjectA" to mapOf("cx_Name" to "Keyword")),
            mapOf("customObjectB" to mapOf("cx_Name" to "Keyword"))
        ),
        "updateSomeFields" to mapOf(
            // For adding cases where you want to update some values in the Customers Object.
            "bmi" to 25,
            "weight" to 55
        )
    )
)

// Call cxIdentify method
ConnectXManager.cxIdentify(payload)
```

### 5. Open Ticket

```kotlin
val payload = mapOf(
    "key" to "cx_Name",
    "customers" to mapOf(
        "cx_Name" to "CUSTOMER_NAME",
        "cx_firstName" to "CUSTOMER_FIRST_NAME",
        "cx_mobilePhone" to "CUSTOMER_MOBILE_PHONE",
        "cx_email" to "CUSTOMER_EMAIL"
        //... Other Customer Field
    ),
    "ticket" to mapOf(
        "cx_subject" to "EMAIL_SUBJECT",
        "cx_socialAccountName" to "xxxx@hotmail.com",
        "cx_socialContact" to "xxxx@hotmail.com",
        "cx_channel" to "email",
        "email" to mapOf(
            "text" to "text",
            "html" to "<b>EMAIL_CONTENT</b>"
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

ConnectXManager.cxOpenTicket(payload)
```

### 6. Create or Update Custom records

To create a new custom object, you must generate a unique referenceId to identify the record. If you pass a docId, the object is updated instead of being created.

```kotlin
val data = listOf(
    mapOf(
        "attributes" to mapOf("referenceId" to "UNIQUE_ID"), // Replace with your unique ID generation logic
        "cx_Name" to cxName, // Replace cxName with your actual variable
        "docId" to docId // Replace docId with your actual variable
    )
)

ConnectXManager.cxCreateRecord(objectName, data)

```

### 7. Get Unknown ID

This method generates and returns a unique identifier.

```kotlin
val unknownId = ConnectXManager.getUnknownId()
```

## License

[![Apache License](https://img.shields.io/badge/License-Apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![](https://jitpack.io/v/connect-x-team/connect-x-kotlin-sdk.svg)](https://jitpack.io/#connect-x-team/connect-x-kotlin-sdk)
