package com.simprints.id.orchestrator.steps.core.requests

import kotlinx.parcelize.Parcelize

@Parcelize
class AskConsentRequest(val consentType: ConsentType): CoreRequest

enum class ConsentType {
    ENROL,
    IDENTIFY,
    VERIFY
}
