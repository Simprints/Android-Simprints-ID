package com.simprints.feature.externalcredential.screens.select

data class LayoutConfig(
    val layoutStyle: LayoutStyle,
    val screenTitle: String?,
    val topTitle: String?,
    val isTopTitleVisible: Boolean,
    val bottomTitle: String?,
    val isBottomTitleVisible: Boolean,
    val uiText: UiText
)

data class UiText(
    val qrText: String,
    val anyDocText: String,
    val ghanaCardText: String,
    val nhisCardText: String,
)

enum class LayoutStyle {
    Grid, Vertical
}
