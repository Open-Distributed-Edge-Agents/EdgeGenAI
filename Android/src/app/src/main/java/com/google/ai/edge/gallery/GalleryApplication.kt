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

package com.google.ai.edge.gallery

import android.app.Application
import com.google.ai.edge.gallery.common.writeLaunchInfo
import com.google.ai.edge.gallery.data.DataStoreRepository
import com.google.ai.edge.gallery.ui.theme.ThemeSettings
import com.google.ai.edge.gallery.data.ObjectBox
import com.google.ai.edge.gallery.data.SystemPrompt
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import io.objectbox.kotlin.boxFor
import javax.inject.Inject

@HiltAndroidApp
class GalleryApplication : Application() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate() {
        super.onCreate()

        writeLaunchInfo(context = this)

        // Load saved theme.
        ThemeSettings.themeOverride.value = dataStoreRepository.readTheme()

        FirebaseApp.initializeApp(this)
        ObjectBox.init(this)
        addInitialSystemPrompts()
    }

    private fun addInitialSystemPrompts() {
        val systemPromptBox = ObjectBox.store.boxFor<SystemPrompt>()
        if (systemPromptBox.isEmpty) {
            systemPromptBox.put(
                SystemPrompt(
                    role = "Commander",
                    prompt = getString(R.string.system_prompt_commander)
                )
            )
            systemPromptBox.put(
                SystemPrompt(
                    role = "Agent",
                    prompt = getString(R.string.system_prompt_agent)
                )
            )
        }
    }
}
