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

package com.google.ai.edge.gallery.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.R
import kotlinx.coroutines.launch

/** A composable for Terms of Service sheet, shown once when app is launched. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TosSheet(onTosAccepted: () -> Unit, viewingMode: Boolean = false) {
  var viewFullTerms by remember { mutableStateOf(viewingMode) }
  var accepted by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val sheetState =
    rememberModalBottomSheetState(
      skipPartiallyExpanded = true,
      // Don't allow manually hide the sheet.
      confirmValueChange = { targetValue ->
        viewingMode || accepted || (targetValue != SheetValue.Hidden)
      },
    )
  ModalBottomSheet(
    onDismissRequest = { if (viewingMode) onTosAccepted() },
    sheetState = sheetState,
    modifier = Modifier.wrapContentHeight(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      // Title.
      Text(
        "${stringResource(R.string.tos_sheet_title_app_name)}\n${stringResource(R.string.tos_sheet_title_tos)}",
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).padding(horizontal = 24.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
      )

      Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, fill = false)) {
        // Short content.
        MarkdownText(
          "By using Distributed Edge Agents, you accept (1) the [Google Terms of Service](https://policies.google.com/terms), and (2) Distributed Edge Agents App Terms of Service below.",
          modifier = Modifier.padding(top = 16.dp),
        )

        // Long content.
        if (viewFullTerms) {
          MarkdownText(
            "In addition, your use of any Gemma models in the Distributed Edge Agents app is governed by the [Gemma Terms of Use](https://ai.google.dev/gemma/terms), including the [Gemma Prohibited Use Policy](https://ai.google.dev/gemma/prohibited_use_policy). By using, reproducing, modifying, distributing, performing, or displaying any portion or element of Gemma or any Gemma model derivatives, you agree to be bound by [those terms](https://ai.google.dev/gemma/terms) and that policy.\n" +
              "\n" +
              "Your use of any other AI models in Distributed Edge Agents is subject to the terms and conditions that apply to that model. Please read those terms carefully before using any third-party model.\n" +
              "\n" +
              "Distributed Edge Agents may collect anonymous usage data about your use of the app and share such data with Google. We encourage you to read our [Privacy Policy](https://policies.google.com/privacy). It explains what information we collect, why we collect it, and how you can update, manage, export, and delete your information.",
            modifier = Modifier.padding(top = 14.dp),
          )
        }
      }

      // Toggle to view full terms.
      if (!viewFullTerms) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable { viewFullTerms = true },
        ) {
          Text(
            stringResource(R.string.tos_sheet_view_full_tos),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          )
          Icon(
            Icons.Filled.KeyboardArrowDown,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "",
          )
        }
      }

      // Accept button.
      Button(
        onClick = {
          accepted = true
          scope.launch {
            sheetState.hide()
            onTosAccepted()
          }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 24.dp),
      ) {
        Text(
          stringResource(
            if (viewingMode) R.string.close else R.string.tos_sheet_view_accept_button_label
          )
        )
      }
    }
  }
}
