package com.simprints.feature.dashboard.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.store.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val consentRequired: LiveData<Boolean>
        get() = _consentRequired
    private val _consentRequired = MutableLiveData<Boolean>()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        _consentRequired.postValue(configRepository.getProjectConfiguration().consent.collectConsent)
    }
}
