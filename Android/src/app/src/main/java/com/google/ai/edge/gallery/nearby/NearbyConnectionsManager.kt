/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.nearby

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.ai.edge.gallery.crypto.CryptoManager
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

private const val TAG = "NearbyConnectionsManager"
private const val SERVICE_ID = "com.google.ai.edge.gallery.SERVICE_ID"

@Singleton
class NearbyConnectionsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context)
    }

    private var isAdvertising = false
    private var isDiscovering = false

    private val connectedEndpoints = mutableMapOf<String, String>()

    var onMessageReceived: ((String, String, Boolean, String) -> Unit)? = null
    var onEndpointConnected: ((String) -> Unit)? = null
    var onEndpointDisconnected: ((String) -> Unit)? = null

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes() ?: return
                val signedPayload = Json.decodeFromString(SignedPayload.serializer(), String(receivedBytes))
                val isSignatureValid = cryptoManager.verify(signedPayload.alias, signedPayload.message.toByteArray(), signedPayload.signature)
                val isIdentityValid = connectedEndpoints[endpointId] == signedPayload.alias
                onMessageReceived?.invoke(endpointId, signedPayload.message, isSignatureValid && isIdentityValid, signedPayload.recipient)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Bytes payload fully received.
        }
    }

    var onImpersonationDetected: ((String) -> Unit)? = null
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Automatically accept the connection on both sides.
            val endpointName = connectionInfo.endpointName
//            if (connectedEndpoints.containsValue(endpointName)) {
//                onImpersonationDetected?.invoke(endpointName)
//                connectionsClient.rejectConnection(endpointId)
//                return
//            }
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            connectedEndpoints[endpointId] = endpointName
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d(TAG, "Connection successful with $endpointId")
                onEndpointConnected?.invoke(endpointId)
            } else {
                Log.d(TAG, "Connection failed with $endpointId")
                connectedEndpoints.remove(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from $endpointId")
            connectedEndpoints.remove(endpointId)
            onEndpointDisconnected?.invoke(endpointId)
            if (endpointId == "Commander") {
                onCommanderDisconnected()
            }
        }
    }

    private var agentName: String? = null
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found: $endpointId")
            if (discoveredEndpointInfo.endpointName == "Commander") {
                onOriginalCommanderFound()
            }
            connectionsClient.requestConnection(agentName ?: "Subordinate", endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Endpoint lost: $endpointId")
        }
    }

    fun startAdvertising(endpointName: String) {
        if (isAdvertising) {
            Log.d(TAG, "Already advertising")
            return
        }

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(
            endpointName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Started advertising")
            isAdvertising = true
        }.addOnFailureListener {
            Log.e(TAG, "Failed to start advertising", it)
        }
    }

    fun stopAdvertising() {
        if (!isAdvertising) {
            Log.d(TAG, "Not advertising")
            return
        }
        connectionsClient.stopAdvertising()
        isAdvertising = false
        Log.d(TAG, "Stopped advertising")
    }

    fun startDiscovery(agentName: String?) {
        this.agentName = agentName
        if (isDiscovering) {
            Log.d(TAG, "Already discovering")
            return
        }

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Started discovering")
            isDiscovering = true
        }.addOnFailureListener {
            Log.e(TAG, "Failed to start discovering", it)
        }
    }

    fun stopDiscovery() {
        if (!isDiscovering) {
            Log.d(TAG, "Not discovering")
            return
        }
        connectionsClient.stopDiscovery()
        isDiscovering = false
        Log.d(TAG, "Stopped discovering")
    }

    fun sendMessage(endpointId: String, message: String, alias: String, recipient: String) {
        val signature = cryptoManager.sign(alias, message.toByteArray())
        val signedPayload = SignedPayload(message, signature, alias, recipient)
        val payload = Payload.fromBytes(Json.encodeToString(SignedPayload.serializer(), signedPayload).toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }

    fun broadcastMessage(message: String, alias: String) {
        val signature = cryptoManager.sign(alias, message.toByteArray())
        val signedPayload = SignedPayload(message, signature, alias, "everyone")
        val payload = Payload.fromBytes(Json.encodeToString(SignedPayload.serializer(), signedPayload).toByteArray())
        connectionsClient.sendPayload(connectedEndpoints.keys.toList(), payload)
    }

    fun getConnectedEndpoints(): List<String> {
        return connectedEndpoints.keys.toList()
    }

    fun stopAllEndpoints() {
        for (endpointId in connectedEndpoints.keys) {
            connectionsClient.disconnectFromEndpoint(endpointId)
        }
        connectedEndpoints.clear()
        stopAdvertising()
        stopDiscovery()
    }

    fun onCommanderDisconnected() {
        // If I am the next in line, I will become the new commander.
        // Otherwise, I will start discovering for a new commander.
        val sortedEndpoints = connectedEndpoints.keys.sorted()
        if (sortedEndpoints.isNotEmpty() && sortedEndpoints.first() == myEndpointId) {
            startAdvertising("Commander")
        } else {
            startDiscovery(myEndpointId)
        }
    }

    private val myEndpointId: String by lazy {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        Log.d(TAG, "Device ID: $deviceId")
        deviceId
    }

    fun onOriginalCommanderFound() {
        // If I am a temporary commander, I will stop advertising and start discovering.
        // Otherwise, I will disconnect from the temporary commander and connect to the original one.
        if (isAdvertising) {
            stopAdvertising()
            startDiscovery(myEndpointId)
        } else {
            stopAllEndpoints()
            startDiscovery(myEndpointId)
        }
    }
}
