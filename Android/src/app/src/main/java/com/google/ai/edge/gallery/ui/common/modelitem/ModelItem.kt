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

package com.google.ai.edge.gallery.ui.common.modelitem

// import androidx.compose.ui.tooling.preview.Preview
// import com.google.ai.edge.gallery.ui.preview.MODEL_TEST1
// import com.google.ai.edge.gallery.ui.preview.MODEL_TEST2
// import com.google.ai.edge.gallery.ui.preview.MODEL_TEST3
// import com.google.ai.edge.gallery.ui.preview.MODEL_TEST4
// import com.google.ai.edge.gallery.ui.preview.PreviewModelManagerViewModel
// import com.google.ai.edge.gallery.ui.preview.TASK_TEST1
// import com.google.ai.edge.gallery.ui.preview.TASK_TEST2
// import com.google.ai.edge.gallery.ui.theme.GalleryTheme

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.ModelDownloadStatusType
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.ui.common.DownloadAndTryButton
import com.google.ai.edge.gallery.ui.common.MarkdownText
import com.google.ai.edge.gallery.ui.common.TaskIcon
import com.google.ai.edge.gallery.ui.common.checkNotificationPermissionAndStartDownload
import com.google.ai.edge.gallery.ui.common.getTaskBgColor
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel

private val DEFAULT_VERTICAL_PADDING = 16.dp

/**
 * Composable function to display a model item in the model manager list.
 *
 * This function renders a card representing a model, displaying its task icon, name, download
 * status, and providing action buttons. It supports expanding to show a model description and
 * buttons for learning more (opening a URL) and downloading/trying the model.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelItem(
  model: Model,
  task: Task,
  modelManagerViewModel: ModelManagerViewModel,
  onModelClicked: (Model) -> Unit,
  modifier: Modifier = Modifier,
  onConfigClicked: () -> Unit = {},
  verticalSpacing: Dp = DEFAULT_VERTICAL_PADDING,
  showDeleteButton: Boolean = true,
  showConfigButtonIfExisted: Boolean = false,
) {
  val context = LocalContext.current
  val modelManagerUiState by modelManagerViewModel.uiState.collectAsState()
  val downloadStatus by remember {
    derivedStateOf { modelManagerUiState.modelDownloadStatus[model.name] }
  }
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      modelManagerViewModel.downloadModel(task = task, model = model)
    }

  val boxModifier =
    modifier.fillMaxWidth().clip(RoundedCornerShape(size = 42.dp)).background(getTaskBgColor(task))

  Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
      Modifier.fillMaxWidth().padding(vertical = verticalSpacing, horizontal = 18.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        TaskIcon(task = task)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
        ) {
          ModelItemActionButton(
            context = context,
            model = model,
            task = task,
            modelManagerViewModel = modelManagerViewModel,
            downloadStatus = downloadStatus,
            onDownloadClicked = { model ->
              checkNotificationPermissionAndStartDownload(
                context = context,
                launcher = launcher,
                modelManagerViewModel = modelManagerViewModel,
                task = task,
                model = model,
              )
            },
            showDeleteButton = showDeleteButton,
            showDownloadButton = false,
          )
        }
      }
      ModelNameAndStatus(
        model = model,
        task = task,
        downloadStatus = downloadStatus
      )
      if (model.info.isNotEmpty()) {
        MarkdownText(
          model.info,
        )
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (model.learnMoreUrl.isNotEmpty()) {
          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_VIEW, model.learnMoreUrl.toUri())
              context.startActivity(intent)
            }
          ) {
            Text("Learn More", maxLines = 1)
          }
        }
        val needToDownloadFirst =
          downloadStatus?.status == ModelDownloadStatusType.NOT_DOWNLOADED ||
                  downloadStatus?.status == ModelDownloadStatusType.FAILED
        DownloadAndTryButton(
          task = task,
          model = model,
          enabled = true,
          needToDownloadFirst = needToDownloadFirst,
          modelManagerViewModel = modelManagerViewModel,
          onClicked = { onModelClicked(model) },
        )
      }
    }
  }
}

// @Preview(showBackground = true)
// @Composable
// fun PreviewModelItem() {
//   GalleryTheme {
//     Column(
//       verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)
//     ) {
//       ModelItem(
//         model = MODEL_TEST1,
//         task = TASK_TEST1,
//         onModelClicked = { },
//         modelManagerViewModel = PreviewModelManagerViewModel(context = LocalContext.current),
//       )
//       ModelItem(
//         model = MODEL_TEST2,
//         task = TASK_TEST1,
//         onModelClicked = { },
//         modelManagerViewModel = PreviewModelManagerViewModel(context = LocalContext.current),
//       )
//       ModelItem(
//         model = MODEL_TEST3,
//         task = TASK_TEST2,
//         onModelClicked = { },
//         modelManagerViewModel = PreviewModelManagerViewModel(context = LocalContext.current),
//       )
//       ModelItem(
//         model = MODEL_TEST4,
//         task = TASK_TEST2,
//         onModelClicked = { },
//         modelManagerViewModel = PreviewModelManagerViewModel(context = LocalContext.current),
//       )
//     }
//   }
// }
