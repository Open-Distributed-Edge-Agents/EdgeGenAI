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

package com.google.ai.edge.gallery.crypto

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import com.google.ai.edge.gallery.data.MAX_SUBORDINATE_COUNT

@Singleton
class CryptoManager @Inject constructor() {

    private val keyPairs = mutableMapOf<String, KeyPair>()

    init {
        generateKeyPair("Commander")
        for (i in 1..MAX_SUBORDINATE_COUNT) {
            generateKeyPair("Agent$i")
        }
    }

    private fun generateKeyPair(alias: String) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairs[alias] = keyPairGenerator.generateKeyPair()
    }

    fun getPublicKey(alias: String): PublicKey? {
        return keyPairs[alias]?.public
    }

    fun sign(alias: String, data: ByteArray): ByteArray {
        val privateKey = keyPairs[alias]?.private
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    fun verify(alias: String, data: ByteArray, signature: ByteArray): Boolean {
        val publicKey = getPublicKey(alias)
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(publicKey)
        sig.update(data)
        return sig.verify(signature)
    }
}
