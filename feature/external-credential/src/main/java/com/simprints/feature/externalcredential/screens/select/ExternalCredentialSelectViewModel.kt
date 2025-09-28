package com.simprints.feature.externalcredential.screens.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExternalCredentialSelectViewModel @Inject internal constructor(
    private val configManager: ConfigManager,
) : ViewModel() {

    val externalCredentialTypes: LiveData<List<ExternalCredentialType>>
        get() = _externalCredentialTypes
    private val _externalCredentialTypes = MutableLiveData<List<ExternalCredentialType>>()

    fun loadExternalCredentials() {
        viewModelScope.launch {
            val config = configManager.getProjectConfiguration()
            val allowedExternalCredentials = config.multifactorId?.allowedExternalCredentials.orEmpty()
            _externalCredentialTypes.postValue(allowedExternalCredentials)
            // TODO remove
            _externalCredentialTypes.postValue(ExternalCredentialType.entries)
        }
    }
}
