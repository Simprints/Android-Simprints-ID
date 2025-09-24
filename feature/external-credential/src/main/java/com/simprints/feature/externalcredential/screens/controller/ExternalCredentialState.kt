package com.simprints.feature.externalcredential.screens.controller

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType

internal data class ExternalCredentialState(
    val subjectId: String?,
    val flowType: FlowType,
    val credentialValue: String?,
    val selectedType: ExternalCredentialType?
) {
    companion object {
        val EMPTY = ExternalCredentialState(
            subjectId = null,
            flowType = FlowType.VERIFY,
            credentialValue = null,
            selectedType = null,
        )
    }
}
