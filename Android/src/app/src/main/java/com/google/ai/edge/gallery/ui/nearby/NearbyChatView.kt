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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.ui.llmchat.LlmChatViewModel
import com.google.ai.edge.gallery.ui.common.chat.ChatView
import com.google.ai.edge.gallery.ui.common.chat.ChatMessageText
import com.google.ai.edge.gallery.ui.common.chat.ChatViewModel
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

@Composable
fun NearbyChatView(
    modelManagerViewModel: ModelManagerViewModel,
    viewModel: LlmChatViewModel,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit
) {
    var isCommon by remember { mutableStateOf(false) }
    var recipient by remember { mutableStateOf("everyone") }

    ChatView(
        task = viewModel.curTask,
        modelManagerViewModel = modelManagerViewModel,
        viewModel = viewModel as ChatViewModel,
        onSendMessage = { model, messages ->
            for (message in messages) {
                viewModel.addMessage(model = model, message = message)
                if (message is ChatMessageText) {
                    viewModel.sendMessage(message.content, isCommon, recipient)
                }
            }
        },
        onRunAgainClicked = { model, message ->
            if (message is ChatMessageText) viewModel.runAgain(model, message, {})
        },
        onBenchmarkClicked = { model, message, _, _ ->
            // No-op
        },
        navigateUp = navigateUp,
        bottomContent = {
            val imageUri by viewModel.imageUri.collectAsState()

            Column {
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                    )
                }
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCommon,
                        onCheckedChange = { isCommon = it }
                    )
                    Text("Common question")
                    // Add a dropdown menu to select the recipient
                }
            }
        }
    )
}
