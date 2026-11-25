package com.simprints.feature.dashboard.main.dailyactivity

internal data class DailyActivityUiState(
    // null: no load finished yet, the card keeps its XML default (gone).
    val activity: DailyActivityUi? = null,
) {
    internal data class DailyActivityUi(
        val enrolments: Int,
        val identifications: Int,
        val verifications: Int,
        // Captured at load time; every resume triggers a load.
        val currentDate: String,
    ) {
        val hasEnrolments: Boolean get() = enrolments > 0
        val hasIdentifications: Boolean get() = identifications > 0
        val hasVerifications: Boolean get() = verifications > 0

        val hasNoActivity: Boolean get() = !hasEnrolments && !hasIdentifications && !hasVerifications

        val isEnrolmentsDividerVisible: Boolean get() = hasEnrolments && (hasIdentifications || hasVerifications)

        val isIdentificationsDividerVisible: Boolean get() = hasIdentifications && hasVerifications
    }
}

internal sealed interface DailyActivityUiAction {
    data object Load : DailyActivityUiAction
}

internal sealed interface DailyActivityUiEffect
