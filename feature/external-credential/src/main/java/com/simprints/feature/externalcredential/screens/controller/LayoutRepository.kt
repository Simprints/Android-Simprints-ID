package com.simprints.feature.externalcredential.screens.controller

import com.simprints.feature.externalcredential.screens.select.LayoutConfig
import com.simprints.feature.externalcredential.screens.select.LayoutStyle
import com.simprints.feature.externalcredential.screens.select.UiText
import javax.inject.Inject
import javax.inject.Singleton

private var config = LayoutConfig(
    layoutStyle = LayoutStyle.Grid,
    screenTitle = "Scan patient document",
    topTitle = "Custom",
    bottomTitle = "National ID card",
    isTopTitleVisible = false,
    isBottomTitleVisible = true,
    isDobVisible = false,
    uiText = UiText(
        qrText = "Scan QR",
        anyDocText = "Any document",
        ghanaCardText = "\uD83C\uDDEC\uD83C\uDDED ID Card",
        nhisCardText = "\uD83C\uDFE5 NHIS Card",
    )
)

@Singleton
class LayoutRepository @Inject constructor() {
    var onConfigUpdated: (LayoutConfig) -> Unit = {}
    fun getConfig(): LayoutConfig = config
    fun setConfig(newConfig: LayoutConfig) {
        config = newConfig
        onConfigUpdated(config)
    }
}
