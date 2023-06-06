package com.simprints.feature.fetchsubject.screen

import com.simprints.infra.config.domain.models.GeneralConfiguration

internal sealed class FetchSubjectState {

    object FoundLocal : FetchSubjectState()
    object FoundRemote : FetchSubjectState()
    object NotFound : FetchSubjectState()
    object ConnectionError : FetchSubjectState()

    data class ShowExitForm(val modalities: List<GeneralConfiguration.Modality>): FetchSubjectState()
}
