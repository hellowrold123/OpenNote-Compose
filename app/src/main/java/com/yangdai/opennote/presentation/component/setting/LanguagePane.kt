package com.yangdai.opennote.presentation.component.setting


import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.dialog.LanguageListDialog
import com.yangdai.opennote.presentation.util.Constants.LANGUAGE_SUPPORT

//note 语言界面
@Composable
fun LanguagePane(){
    val context = LocalContext.current
    var isLanguageDialogVisible by remember { mutableStateOf(false) }
    Column(
        Modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



        Spacer(modifier = Modifier.height(16.dp))


        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {

                    isLanguageDialogVisible=true

                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Language, contentDescription = "language"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.select_language)) })

        Spacer(Modifier.navigationBarsPadding())
    }
    if(isLanguageDialogVisible){
        OnLanguageClicked({ isLanguageDialogVisible = false })
    }


}


fun getSupportedLanguages(): Map<String, String> {
    val localeList = LocaleListCompat.forLanguageTags(LANGUAGE_SUPPORT)
    val map = mutableMapOf<String, String>()

    for (a in 0 until localeList.size()) {
        localeList[a].let {
            it?.let { it1 -> map.put(it1.getDisplayName(it), it.toLanguageTag()) }
        }
    }
    return map
}

@Composable
private fun OnLanguageClicked( onExit: () -> Unit) {
    val context = LocalContext.current
    val languages = getSupportedLanguages().toList()
    LanguageListDialog(
        text = stringResource(R.string.language),
        list = languages,
        onExit = onExit,
        extractDisplayData = { it },
        initialItem = Pair(context.getString(R.string.system_language), second = ""),
        setting = { isFirstItem, isLastItem, displayData ->
            SettingsBox(
                size = 8.dp,
                title = displayData.first,
                actionType = ActionType.RADIOBUTTON,
                variable = if (displayData.second.isNotBlank()) {
                    AppCompatDelegate.getApplicationLocales()[0]?.language == displayData.second
                } else {
                    AppCompatDelegate.getApplicationLocales().isEmpty
                },
                switchEnabled = { if (displayData.second.isNotBlank()) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(displayData.second))
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
                }
            )
        }
    )
}