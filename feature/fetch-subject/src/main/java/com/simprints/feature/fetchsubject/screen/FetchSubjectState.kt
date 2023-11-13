package com.simprints.feature.fetchsubject.screen

import com.simprints.infra.config.store.models.GeneralConfiguration

internal sealed class FetchSubjectState {

    data object FoundLocal : FetchSubjectState()
    data object FoundRemote : FetchSubjectState()
    data object NotFound : FetchSubjectState()
    data object ConnectionError : FetchSubjectState()

    data class ShowExitForm(val modalities: List<GeneralConfiguration.Modality>): FetchSubjectState()
}
