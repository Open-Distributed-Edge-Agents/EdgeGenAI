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
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NearbyConnectionsManager"
private const val SERVICE_ID = "com.google.ai.edge.gallery.SERVICE_ID"

@Singleton
class NearbyConnectionsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context)
    }

    private var isAdvertising = false
    private var isDiscovering = false

    private val connectedEndpoints = mutableMapOf<String, String>()

    var onMessageReceived: ((String, String) -> Unit)? = null
    var onEndpointConnected: ((String) -> Unit)? = null
    var onEndpointDisconnected: ((String) -> Unit)? = null

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val receivedBytes = payload.asBytes() ?: return
                val message = String(receivedBytes)
                onMessageReceived?.invoke(endpointId, message)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Bytes payload fully received.
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // Automatically accept the connection on both sides.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            connectedEndpoints[endpointId] = connectionInfo.endpointName
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

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found: $endpointId")
            connectionsClient.requestConnection("Subordinate", endpointId, connectionLifecycleCallback)
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

    fun startDiscovery() {
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

    fun sendMessage(endpointId: String, message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }

    fun broadcastMessage(message: String) {
        val payload = Payload.fromBytes(message.toByteArray())
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
        if (sortedEndpoints.isNotEmpty() && sortedEndpoints.first() == getMyEndpointId()) {
            startAdvertising("Commander")
        } else {
            startDiscovery()
        }
    }

    private fun getMyEndpointId(): String {
        // This is a placeholder. In a real app, you would need to get the endpoint ID of the current device.
        return "MyEndpointId"
    }
}
