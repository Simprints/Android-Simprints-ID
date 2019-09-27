package com.simprints.id.domain.moduleapi.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
class AskConsentRequest(val consentType: ConsentType): CoreRequest

enum class ConsentType {
    ENROL,
    IDENTIFY,
    VERIFY
}
