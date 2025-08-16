package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.R

@Composable
fun MarkNoteDialog(
    markNotes:String,
    maxTagCount:Int,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var isMaxTagCount by remember { mutableStateOf(false) }
    val tags = remember { mutableStateListOf<String>() }
    markNotes.split(Constants.Preferences.TAG_SEPARATOR).forEach { item->if(item.isNotBlank())tags.add(item) }
    var inputText by remember { mutableStateOf("") }
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.mark_note))
        },
        text = {

            Column(modifier = Modifier.fillMaxWidth()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        InputChip(
                            onClick = {
                                tags.remove(tag)
                                      if(tags.count()+1<=maxTagCount){
                                          isMaxTagCount=false
                                      }
                                      }, // 点击删除标签
                            label = { Text(tag) },
                            selected = false,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(InputChipDefaults.AvatarSize)
                                )
                            }
                        )
                    }
                }

                // 输入框（添加新标签）
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        if (inputText.isNotBlank()
                            && tags.count { item -> item.equals(inputText.trim()) } ==0
                            && tags.count()+1<=maxTagCount
                        )
                            isMaxTagCount=false
                            else
                            isMaxTagCount=true

                    },
                    label = { Text(stringResource(R.string.add_tag_label)) },
                    trailingIcon = {
                        if (inputText.isNotBlank()
                            && tags.count { item -> item.equals(inputText.trim()) } ==0
                            && tags.count()+1<=maxTagCount
                            ) {
                            IconButton(onClick = {
                                tags.add(inputText.trim())
                                inputText = ""
                            }) {
                                Icon(Icons.Default.Add, "Add")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()
                                && tags.count { item -> item.equals(inputText.trim()) } ==0
                                && tags.count()+1<=maxTagCount
                                ) {
                                tags.add(inputText.trim())
                                inputText = ""
                            }
                        }
                    )
                )
                if (isMaxTagCount){
                    Text(stringResource(R.string.max_tag_prompt))
                }

            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(onClick = {


            onConfirm(tags.joinToString(Constants.Preferences.TAG_SEPARATOR))
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onDismissRequest()
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    )
}
private fun isShowMaxTagCout(tags:SnapshotStateList<String>,maxTagCount: Int): Boolean {
    return tags.count()+1<=maxTagCount
}
@Composable
@Preview
fun MarkNoteDialogPreview() {
    MarkNoteDialog(
        markNotes = "sdf",
        maxTagCount = 3,
        onDismissRequest = {},
        onConfirm = {}
    )
}