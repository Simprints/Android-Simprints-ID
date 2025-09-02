package com.simprints.feature.enrollast.screen

import com.simprints.core.domain.common.Modality

internal sealed class EnrolLastState {
    data class Success(
        val newGuid: String,
    ) : EnrolLastState()

    data class Failed(
        val errorType: ErrorType,
        val modalities: List<Modality>,
    ) : EnrolLastState()

    enum class ErrorType {
        NO_MATCH_RESULTS,
        DUPLICATE_ENROLMENTS,
        GENERAL_ERROR,
    }
}
