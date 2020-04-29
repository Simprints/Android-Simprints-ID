package com.simprints.id.orchestrator.steps.core.response

import kotlinx.android.parcel.Parcelize

@Parcelize
class AskConsentResponse(val consentData: ConsentResponse): CoreResponse(type = CoreResponseType.CONSENT)

enum class ConsentResponse {
    ACCEPTED,
    DECLINED
}
