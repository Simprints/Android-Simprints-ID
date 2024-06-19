package com.simprints.feature.consent.screens.consent

internal data class ConsentViewState(
    val showLogo: Boolean = true,
    val consentText: String = "",
    val showParentalConsent: Boolean = false,
    val parentalConsentText: String = "",
    val selectedTab: Int = 0
)
