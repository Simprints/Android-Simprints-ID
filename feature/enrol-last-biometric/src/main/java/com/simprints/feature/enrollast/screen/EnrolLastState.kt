package com.simprints.feature.enrollast.screen

import com.simprints.infra.config.store.models.GeneralConfiguration

internal sealed class EnrolLastState {
    data class Success(
        val newGuid: String,
    ) : EnrolLastState()

    data class Failed(
        val errorType: ErrorType,
        val modalities: List<GeneralConfiguration.Modality>,
    ) : EnrolLastState()

    enum class ErrorType {
        NO_MATCH_RESULTS,
        DUPLICATE_ENROLMENTS,
        GENERAL_ERROR,
        EXTERNAL_CREDENTIAL_SAVE_ERROR,
    }
}

internal sealed class ExternalCredentialSaveState {
    abstract val newGuid: String
    data class Success(
        override val newGuid: String,
    ) : ExternalCredentialSaveState()

    data class Failed(
        override val newGuid: String,
    ) : ExternalCredentialSaveState()
}
