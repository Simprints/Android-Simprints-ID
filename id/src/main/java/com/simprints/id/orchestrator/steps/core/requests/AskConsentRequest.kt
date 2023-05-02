package com.simprints.id.orchestrator.steps.core.requests

import com.simprints.feature.consent.ConsentType
import kotlinx.parcelize.Parcelize

@Parcelize
class AskConsentRequest(val consentType: ConsentType): CoreRequest

