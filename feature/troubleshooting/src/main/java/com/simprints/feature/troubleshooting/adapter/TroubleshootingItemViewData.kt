package com.simprints.feature.troubleshooting.adapter

data class TroubleshootingItemViewData(
    val title: String,
    val subtitle: String = "",
    val body: String = "",
    val navigationId: String? = null,
)
