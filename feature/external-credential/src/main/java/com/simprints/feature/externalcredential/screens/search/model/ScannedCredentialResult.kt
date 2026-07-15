package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.step.StepResult
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.randomUUID
import com.simprints.feature.externalcredential.model.BoundingBox
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("ScannedCredentialResult")
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class ScannedCredentialResult(
    val credentialScanId: String = randomUUID(),
    val document: MfidDocument,
    val documentImagePath: String?,
    val zoomedCredentialImagePath: String?,
    val credentialBoundingBox: BoundingBox?,
    val scanStartTime: Timestamp,
    val scanEndTime: Timestamp,
) : StepResult {
    val credential
        get() = document.credential
    val credentialType
        get() = when (document) {
            is MfidDocument.GhanaIdCard -> ExternalCredentialType.GhanaIdCard
            is MfidDocument.GhanaNhisCard -> ExternalCredentialType.NHISCard
            is MfidDocument.GhanaQrCode -> ExternalCredentialType.QRCode
            is MfidDocument.FaydaCard -> ExternalCredentialType.FaydaCard
        }
}
