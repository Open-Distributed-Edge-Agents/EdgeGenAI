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

package com.google.ai.edge.gallery.ui.nearby

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.data.loadMissionDescription
import java.io.InputStream

@Composable
fun NearbyRoleSelectionScreen(
    onRoleSelected: (isCommander: Boolean, agentName: String?) -> Unit
) {
    var selectedAgent by remember { mutableStateOf<Int?>(null) }
    var missionDescription by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Role", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onRoleSelected(true, null) }) {
                Text("Commander")
            }
            (1..5).forEach { agentNumber ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedAgent = agentNumber
                        missionDescription = loadMissionDescription(context, "Agent$agentNumber")
                    }
                ) {
                    Checkbox(
                        checked = selectedAgent == agentNumber,
                        onCheckedChange = {
                            selectedAgent = agentNumber
                            missionDescription = loadMissionDescription(context, "Agent$agentNumber")
                        }
                    )
                    Text("Agent $agentNumber")
                }
            }
            Button(
                onClick = { onRoleSelected(false, "Agent$selectedAgent") },
                enabled = selectedAgent != null
            ) {
                Text("Subordinate")
            }

            missionDescription?.let {
                MissionDetails(missionDescription = it)
            }
        }
    }
}

@Composable
fun MissionDetails(missionDescription: String) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Mission Details", style = MaterialTheme.typography.titleLarge)
        Text(missionDescription)
    }
}