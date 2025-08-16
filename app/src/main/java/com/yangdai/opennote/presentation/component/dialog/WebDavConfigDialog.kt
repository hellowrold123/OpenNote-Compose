package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

//note webdav弹窗
@Composable
fun WebDavConfigDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String,String,String) -> Unit,
) {
    var webDAVUrl  by remember { mutableStateOf("https://dav.jianguoyun.com/dav/") }
    var webDAVAccount by remember { mutableStateOf("") }
    var webDAVPassword by remember { mutableStateOf("") }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.webdav_title))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = webDAVUrl,
                    onValueChange = {
                        webDAVUrl=it
                    },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.webdav_linkurl)) })

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = webDAVAccount,
                    onValueChange = {
                        webDAVAccount=it
                    },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.webdav_username)) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = webDAVPassword,
                    onValueChange = {
                        webDAVPassword=it
                    },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.webdav_password)) },

                )
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    onConfirm(webDAVUrl,webDAVAccount,webDAVPassword)
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}