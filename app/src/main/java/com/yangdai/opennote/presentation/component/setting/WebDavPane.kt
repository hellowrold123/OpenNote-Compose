package com.yangdai.opennote.presentation.component.setting

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.presentation.component.dialog.ProgressDialog
import com.yangdai.opennote.presentation.component.dialog.WebDavConfigDialog
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.Constants.WebDavConfigInfo.WEBDAV_FILE_DIRNAME
import com.yangdai.opennote.presentation.util.Constants.WebDavConfigInfo.WEBDAV_FILE_MIMETYPE
import com.yangdai.opennote.presentation.util.Constants.WebDavConfigInfo.WEBDAV_FILE_SUFFIX
import com.yangdai.opennote.presentation.util.SyncManager
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.log

@Composable
fun WebDavPane(sharedViewModel: SharedViewModel){
    val webDavConfigState by sharedViewModel.webDavConfigStateFlow.collectAsStateWithLifecycle()
    var isWebDavDialogVisible by remember { mutableStateOf(false) }
    var isLoadingProgress by remember { mutableStateOf(false) }
    var msgProgress by remember { mutableStateOf("") }
    var pProgress by remember { mutableStateOf(0.0f) }

    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    val syncManager = SyncManager(webDavConfigState)
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        //todo 参数使用
        val ss = innerPadding
        Column(
            Modifier
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val scope = rememberCoroutineScope()
            Spacer(modifier = Modifier.height(16.dp))


            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.CloudSync,
                        contentDescription = "Password"
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.webdav_title)) },
                trailingContent = {
                    Switch(
                        checked = webDavConfigState.webDavLoginSuccess,
                        onCheckedChange = { checked ->
                            if (checked){
                                isWebDavDialogVisible = true
                            }
                            else{
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_URL,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_USERNAME,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_PASSWORD,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_IS_LOGIN_SUCCESS,
                                    false
                                )
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                            }
                        }
                    )
                },
                supportingContent = {
                }
            )

            AnimatedVisibility(webDavConfigState.webDavLoginSuccess) {
                Column( Modifier
                    .padding(horizontal = 8.dp)) {
                    ListItem(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(CircleShape)
                            .clickable {

                                scope.launch {
                                    isLoadingProgress=true

                                    val uploadWebDav = sharedViewModel.uploadWebDav()
                                    pProgress=0.2f
                                    val currentDateTime = LocalDateTime.now()
                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")
                                    val formattedTime = currentDateTime.format(formatter).replace(" ","_")
                                    val filename="BNote_Back[$formattedTime]"+WEBDAV_FILE_SUFFIX
                                    Log.i("WebDavPane: filename",filename)
                                    val result = syncManager.uploadToWebDAV(
                                        filename,
                                        WEBDAV_FILE_DIRNAME,
                                        uploadWebDav,
                                        WEBDAV_FILE_MIMETYPE
                                    )
                                    pProgress=0.7f
                                    result.fold(
                                        onSuccess = {
                                            pProgress=1.0f
                                        },
                                        onFailure = { e ->
                                            msgProgress=e.toString()
                                        }
                                    )

                                }

                            },
                        colors = ListItemDefaults.colors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Upload, contentDescription = "upload"
                            )
                        },
                        headlineContent = { Text(text = stringResource(R.string.webdav_upload_title)) },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.webdav_upload_desc)
                            )
                        }
                    )
                    ListItem(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(CircleShape)
                            .clickable {
                                scope.launch {
                                    isLoadingProgress = true
                                    pProgress = 0.1f
                                    val result = syncManager.listAllFile(WEBDAV_FILE_DIRNAME)
                                    pProgress = 0.3f
                                    result.fold(
                                        onSuccess = { datas ->
                                            val downResult = syncManager.downloadFileLastTime(datas)
                                            pProgress = 0.5f
                                            downResult.fold(
                                                onSuccess = { inputStream ->
                                                    sharedViewModel.downLoadSingleFileWebDav(
                                                        inputStream
                                                    )
                                                    pProgress = 1.0f
                                                },
                                                onFailure = { exception ->
                                                    msgProgress = exception.toString()
                                                }
                                            )

                                        },
                                        onFailure = { e ->
                                            msgProgress = e.toString()
                                        }
                                    )

                                }


                            },
                        colors = ListItemDefaults.colors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Download, contentDescription = "download"
                            )
                        },
                        headlineContent = { Text(text = stringResource(R.string.webdav_download_title)) },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.webdav_download_desc)
                            )
                        }
                    )
                }


            }

            Spacer(Modifier.navigationBarsPadding())
            if (isWebDavDialogVisible) {
                WebDavConfigDialog(
                    onDismissRequest = { isWebDavDialogVisible = false },
                ) { url, name, pwd ->
                    Log.i("WebDavPane", "WebDavPane:$url#$name#$pwd")
                    isWebDavDialogVisible = false

                    scope.launch {
                        val result = syncManager.checkConnection(url, name, pwd)
                        result.fold(
                            onSuccess = {
                                //note 存储
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_URL,
                                    url
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_USERNAME,
                                    name
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_PASSWORD,
                                    pwd
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_IS_LOGIN_SUCCESS,
                                    true
                                )
                                // If connection is successful, try to list files
                                snackbarHostState.showSnackbar("ok")
                            },
                            onFailure = { exception ->
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_URL,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_USERNAME,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_PASSWORD,
                                    ""
                                )
                                sharedViewModel.putPreferenceValue(
                                    Constants.WebDavConfigInfo.WEBDAV_IS_LOGIN_SUCCESS,
                                    false
                                )
                                snackbarHostState.showSnackbar("failed")
                            }
                        )


                    }


                }
            }

            ProgressDialog(
                isLoading = isLoadingProgress,
                progress = pProgress,
                message = msgProgress,
                onDismissRequest = {
                    isLoadingProgress=false
                }
            )
        }
    }
}