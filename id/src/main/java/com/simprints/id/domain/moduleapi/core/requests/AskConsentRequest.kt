package com.simprints.id.domain.moduleapi.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
class AskConsentRequest(val consentType: ConsentType): CoreRequest {
    companion object {
        const val CONSENT_STEP_BUNDLE = "core_step_bundle"
    }
}

enum class ConsentType {
    ENROL,
    IDENTIFY,
    VERIFY
}
