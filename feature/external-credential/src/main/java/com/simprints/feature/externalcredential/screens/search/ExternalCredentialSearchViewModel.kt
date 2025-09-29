package com.simprints.feature.externalcredential.screens.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.domain.common.FlowType
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.SearchCredentialState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


internal class ExternalCredentialSearchViewModel @AssistedInject constructor(
    @Assisted val scannedCredential: ScannedCredential,
    @Assisted val flowType: FlowType,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(scannedCredential: ScannedCredential, flowType: FlowType): ExternalCredentialSearchViewModel
    }

    private var state: SearchCredentialState = SearchCredentialState.buildInitial(scannedCredential, flowType)
        set(value) {
            field = value
            _stateLiveData.postValue(value)
        }
    private val _stateLiveData = MutableLiveData(state)
    val stateLiveData: LiveData<SearchCredentialState> = _stateLiveData
    private fun updateState(state: (SearchCredentialState) -> SearchCredentialState) {
        this.state = state(this.state)
    }

}
