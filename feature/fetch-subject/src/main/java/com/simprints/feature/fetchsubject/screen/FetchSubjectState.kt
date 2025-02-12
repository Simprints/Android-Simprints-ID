package com.simprints.feature.fetchsubject.screen

internal sealed class FetchSubjectState {
    data object FoundLocal : FetchSubjectState()

    data object FoundRemote : FetchSubjectState()

    data object NotFound : FetchSubjectState()

    data object ConnectionError : FetchSubjectState()

    data object ShowExitForm : FetchSubjectState()
}
