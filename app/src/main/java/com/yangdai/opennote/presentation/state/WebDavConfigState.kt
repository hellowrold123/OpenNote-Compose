package com.yangdai.opennote.presentation.state

import androidx.compose.runtime.Stable

@Stable
data class WebDavConfigState(
    val webDavUrl: String="",
    val webDavUsername: String = "",
    val webDavPassword: String = "",
    val webDavLoginSuccess: Boolean =false
)
