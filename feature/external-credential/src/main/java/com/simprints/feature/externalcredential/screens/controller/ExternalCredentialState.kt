package com.simprints.feature.externalcredential.screens.controller

import com.simprints.core.domain.externalcredential.ExternalCredentialType

internal data class ExternalCredentialState(
    val selectedType: ExternalCredentialType?
) {
    companion object {
        val EMPTY = ExternalCredentialState(
            selectedType = null
        )
    }
}
