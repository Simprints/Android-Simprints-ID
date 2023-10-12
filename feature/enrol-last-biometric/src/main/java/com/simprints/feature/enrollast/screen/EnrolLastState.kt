package com.simprints.feature.enrollast.screen

import com.simprints.infra.config.store.models.GeneralConfiguration

internal sealed class EnrolLastState {
    data class Success(val newGuid: String) : EnrolLastState()
    data class Failed(val modalities: List<GeneralConfiguration.Modality>) : EnrolLastState()
}
