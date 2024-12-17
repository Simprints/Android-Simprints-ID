package com.simprints.feature.dashboard.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val securityManager: SecurityManager,
) : ViewModel() {
    val consentRequired: LiveData<Boolean>
        get() = _consentRequired
    private val _consentRequired = MutableLiveData<Boolean>()

    val rootedDeviceDetected: LiveData<LiveDataEvent>
        get() = _rootedDeviceDetected
    private val _rootedDeviceDetected = MutableLiveData<LiveDataEvent>()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        _consentRequired.postValue(configManager.getProjectConfiguration().consent.collectConsent)
        checkIfDeviceIsSafe()
    }

    private fun checkIfDeviceIsSafe() = try {
        securityManager.checkIfDeviceIsRooted()
    } catch (ex: RootedDeviceException) {
        Simber.e("Rooted device detected", ex)
        _rootedDeviceDetected.send()
    }
}
