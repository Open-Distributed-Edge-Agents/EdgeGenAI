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

package com.google.ai.edge.gallery.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemPromptRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val systemPromptBox = ObjectBox.store.boxFor<SystemPrompt>()

    fun getSystemPrompt(role: String): SystemPrompt? {
        val promptFromDb = systemPromptBox.query(SystemPrompt_.role.equal(role)).build().findFirst()
        if (promptFromDb != null) {
            return promptFromDb
        }

        val promptFileName = when (role) {
            "Commander" -> "commander_system_prompt.md"
            "Agent" -> "agent_system_prompt.md"
            else -> return null
        }

        return try {
            val prompt = context.assets.open(promptFileName).bufferedReader().use { it.readText() }
            SystemPrompt(role = role, prompt = prompt)
        } catch (e: Exception) {
            null
        }
    }

    fun updateSystemPrompt(systemPrompt: SystemPrompt) {
        systemPromptBox.put(systemPrompt)
    }
}
