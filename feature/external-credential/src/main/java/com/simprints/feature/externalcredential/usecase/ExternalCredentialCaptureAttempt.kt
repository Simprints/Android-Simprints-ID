package com.simprints.feature.externalcredential.usecase

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult

internal data class ExternalCredentialCaptureAttempt(
    val scannedCredentialResult: ScannedCredentialResult,
    val externalCredential: ExternalCredential,
    val startTime: Timestamp,
    val endTime: Timestamp,
    val selectionEventId: String,
)
