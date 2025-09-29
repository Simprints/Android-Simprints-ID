package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.domain.common.FlowType

@Keep
internal data class SearchCredentialState(
    val scannedCredential: ScannedCredential,
    val flowType: FlowType,
    val searchState: SearchState,
    val isConfirmed: Boolean,
) {
    companion object {
        fun buildInitial(scannedCredential: ScannedCredential, flowType: FlowType) =
            SearchCredentialState(
                scannedCredential = scannedCredential,
                flowType = flowType,
                searchState = SearchState.Searching,
                isConfirmed = false
            )
    }
}

internal sealed class SearchState {
    data object Searching : SearchState()

    data class SubjectFound(
        val subjectId: String
    ) : SearchState()

    data object SubjectNotFound : SearchState()
}
