package com.simprints.feature.consent.screens.consent

import com.simprints.feature.consent.screens.consent.helpers.GeneralConsentTextHelper
import com.simprints.feature.consent.screens.consent.helpers.ParentalConsentTextHelper

internal data class ConsentViewState(
    val showLogo: Boolean = true,
    val showParentalConsent: Boolean = false,
    val consentTextBuilder: GeneralConsentTextHelper? = null,
    val parentalTextBuilder: ParentalConsentTextHelper? = null,
    val selectedTab: Int = 0,
)
