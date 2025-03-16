package com.xayah.databackup.feature.setup

import android.app.Activity
import android.os.Process
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.xayah.databackup.R
import com.xayah.databackup.ui.component.OnResume
import com.xayah.databackup.ui.component.PermissionCard
import com.xayah.databackup.ui.theme.DataBackupTheme
import com.xayah.databackup.util.CustomSuFile
import com.xayah.databackup.util.KeyCustomSuFile
import com.xayah.databackup.util.navigateSafely
import com.xayah.databackup.util.popBackStackSafely
import com.xayah.databackup.util.readString
import com.xayah.databackup.util.saveString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.size(300.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.img_setup),
                        contentDescription = "Localized description"
                    )
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "DataBackup",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .fillMaxWidth(),
                    text = "Enjoy hassle-free backups with DataBackup. It’s simple to use and completely FOSS. Keep your data safe without the stress",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier
                            .wrapContentSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = DataBackupTheme.greenColorScheme.primary),
                        onClick = {
                            navController.navigateSafely(Permissions(true))
                        }
                    ) {
                        Text("Get started")
                    }
                }
            }

            Spacer(modifier = Modifier.size(innerPadding.calculateBottomPadding()))
        }
    }
}

@Composable
fun CustomSUFileDialog(
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(runBlocking { context.readString(CustomSuFile).first() }))
    }
    var isError by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        title = { Text(text = "Custom SU file") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    isError = it.text.isBlank()
                    text = it
                },
                isError = isError,
                label = { Text("File") },
                supportingText = { Text("Restart app to take effect") }
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                enabled = isError.not(),
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.Default) {
                            context.saveString(KeyCustomSuFile, text.text)
                            onDismissRequest()
                            (context as Activity).finishAffinity()
                            Process.killProcess(Process.myPid())
                            exitProcess(0)
                        }
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun PermissionsScreen(
    navController: NavHostController,
    viewModel: PermissionsViewModel = viewModel(),
    permissions: Permissions,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var openCustomSUFileDialog by remember { mutableStateOf(false) }

    OnResume {
        if (viewModel.isGrantingNotificationPermission) {
            viewModel.checkNotification(context)
            viewModel.isGrantingNotificationPermission = false
        }
    }

    if (openCustomSUFileDialog) {
        CustomSUFileDialog {
            openCustomSUFileDialog = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    text = "Permissions",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 24.dp)
                        .fillMaxWidth(),
                    text = "DataBackup needs the following permissions to work. If denied, some functions will not work properly",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PermissionCard(
                        state = uiState.rootCardProp.state,
                        icon = ImageVector.vectorResource(uiState.rootCardProp.icon),
                        title = uiState.rootCardProp.title,
                        content = uiState.rootCardProp.content,
                        onClick = { viewModel.validateRoot(context) },
                        actionIcon = ImageVector.vectorResource(R.drawable.ic_settings),
                        onActionButtonClick = {
                            openCustomSUFileDialog = true
                        }
                    )

                    PermissionCard(
                        state = uiState.notificationProp.state,
                        icon = ImageVector.vectorResource(uiState.notificationProp.icon),
                        title = uiState.notificationProp.title,
                        content = uiState.notificationProp.content,
                        onClick = { viewModel.validateNotification(context) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    enabled = permissions.enableBackBtn,
                    modifier = Modifier
                        .wrapContentSize(),
                    colors = ButtonDefaults.textButtonColors(contentColor = DataBackupTheme.greenColorScheme.primary),
                    onClick = {
                        navController.popBackStackSafely()
                    }
                ) {
                    Text("Back")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    modifier = Modifier
                        .wrapContentSize()
                        .animateContentSize(),
                    colors = ButtonDefaults.buttonColors(containerColor = DataBackupTheme.greenColorScheme.primary),
                    onClick = { viewModel.onNextButtonClick(context) }
                ) {
                    AnimatedContent(
                        targetState = if (viewModel.allGranted) "Next" else "Grant all",
                        label = "animated content"
                    ) { targetContent ->
                        Text(targetContent)
                    }
                }
            }

            Spacer(modifier = Modifier.size(innerPadding.calculateBottomPadding()))
        }
    }
}
