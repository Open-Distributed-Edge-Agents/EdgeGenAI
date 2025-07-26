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
import java.io.InputStream

fun loadMissionDescription(context: Context, agentName: String): String {
    val agentNumber = agentName.replace("Agent", "").toInt()
    val resourceId = context.resources.getIdentifier(
        "mission_agent_$agentNumber",
        "raw",
        context.packageName
    )
    return try {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        "No mission description found for $agentName"
    }
}
