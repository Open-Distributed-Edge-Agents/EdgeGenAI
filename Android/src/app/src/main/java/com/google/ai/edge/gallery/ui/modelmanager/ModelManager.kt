package com.google.ai.edge.gallery.ui.modelmanager

// import androidx.compose.ui.tooling.preview.Preview
// import com.google.ai.edge.gallery.ui.preview.PreviewModelManagerViewModel
// import com.google.ai.edge.gallery.ui.preview.TASK_TEST1
// import com.google.ai.edge.gallery.ui.theme.GalleryTheme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.GalleryTopAppBar
import com.google.ai.edge.gallery.data.AppBarAction
import com.google.ai.edge.gallery.data.AppBarActionType
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.data.TaskType

/** A screen to manage models. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManager(
    task: Task,
    viewModel: ModelManagerViewModel,
    navigateUp: () -> Unit,
    onModelClicked: (Model, Boolean, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    val isRoleSelected by remember { derivedStateOf { selectedRole != null } }

    // Set title based on the task.
    var title = "${task.type.label} model"
    if (task.models.size != 1) {
        title += "s"
    }
    // Model count.
    val modelCount by remember {
        derivedStateOf {
            val trigger = task.updateTrigger.value
            if (trigger >= 0) {
                task.models.size
            } else {
                -1
            }
        }
    }

    // Navigate up when there are no models left.
    LaunchedEffect(modelCount) {
        if (modelCount == 0) {
            navigateUp()
        }
    }

    // Handle system's edge swipe.
    BackHandler { navigateUp() }

    Scaffold(
        modifier = modifier,
        topBar = {
            GalleryTopAppBar(
                title = title,
                leftAction = AppBarAction(actionType = AppBarActionType.NAVIGATE_UP, actionFn = navigateUp),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (task.type == TaskType.GROUP_CHAT) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Select Role", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedRole = "Commander" }
                    ) {
                        RadioButton(
                            selected = selectedRole == "Commander",
                            onClick = { selectedRole = "Commander" }
                        )
                        Text("Commander")
                    }
                    (1..3).forEach { agentNumber ->
                        val agentRole = "Agent $agentNumber"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedRole = agentRole }
                        ) {
                            RadioButton(
                                selected = selectedRole == agentRole,
                                onClick = { selectedRole = agentRole }
                            )
                            Text(agentRole)
                        }
                    }
                }
            }
            if (task.type != TaskType.GROUP_CHAT || isRoleSelected) {
                ModelList(
                    task = task,
                    modelManagerViewModel = viewModel,
                    onModelClicked = { model ->
                        val isCommander = selectedRole == "Commander"
                        val agentName = if (isCommander) null else selectedRole
                        onModelClicked(model, isCommander, agentName)
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                )
            }
        }
    }
}

// @Preview
// @Composable
// fun ModelManagerPreview() {
//   val context = LocalContext.current

//   GalleryTheme {
//     ModelManager(
//       viewModel = PreviewModelManagerViewModel(context = context),
//       onModelClicked = {},
//       task = TASK_TEST1,
//       navigateUp = {},
//     )
//   }
// }
