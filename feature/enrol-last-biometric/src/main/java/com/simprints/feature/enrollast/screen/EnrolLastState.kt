package com.simprints.feature.enrollast.screen

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential

@ExcludedFromGeneratedTestCoverageReports("Data class")
internal sealed class EnrolLastState {
    @ExcludedFromGeneratedTestCoverageReports("Data class")
    data class Success(
        val newGuid: String,
        val externalCredential: ExternalCredential?,
    ) : EnrolLastState()

    @ExcludedFromGeneratedTestCoverageReports("Data class")
    data class Failed(
        val errorType: ErrorType,
        val modalities: List<Modality>,
    ) : EnrolLastState()

    @ExcludedFromGeneratedTestCoverageReports("Data class")
    enum class ErrorType {
        NO_MATCH_RESULTS,
        DUPLICATE_ENROLMENTS,
        GENERAL_ERROR,
    }
}
