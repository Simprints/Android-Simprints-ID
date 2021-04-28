package com.simprints.face.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.license.repository.LicenseRepository
import com.simprints.id.data.license.repository.LicenseState
import com.simprints.id.data.license.repository.LicenseVendor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class ConfigurationViewModel(
    private val licenseRepository: LicenseRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    val configurationState: MutableLiveData<LiveDataEventWithContent<ConfigurationState>> = MutableLiveData()

    fun retrieveLicense(projectId: String, deviceId: String) = viewModelScope.launch {
        licenseRepository.getLicenseStates(projectId, deviceId, LicenseVendor.RANK_ONE_FACE)
            .flowOn(dispatcherProvider.io())
            .map { it.toConfigurationState() }
            .collect { configurationState.send(it) }
    }

    fun deleteInvalidLicense() {
        Timber.d("License is invalid, deleting it")
        licenseRepository.deleteCachedLicense()
    }

    private fun LicenseState.toConfigurationState(): ConfigurationState =
        when (this) {
            LicenseState.Started -> ConfigurationState.Started
            LicenseState.Downloading -> ConfigurationState.Downloading
            is LicenseState.FinishedWithSuccess -> ConfigurationState.FinishedWithSuccess(license)
            is LicenseState.FinishedWithError -> ConfigurationState.FinishedWithError(errorCode)
        }
}
