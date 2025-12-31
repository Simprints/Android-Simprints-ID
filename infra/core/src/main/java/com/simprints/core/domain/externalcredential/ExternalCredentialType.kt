package com.simprints.core.domain.externalcredential

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.serialization.Serializable

@ExcludedFromGeneratedTestCoverageReports("enum")
@Serializable
enum class ExternalCredentialType {
    NHISCard,
    GhanaIdCard,
    QRCode,
}
