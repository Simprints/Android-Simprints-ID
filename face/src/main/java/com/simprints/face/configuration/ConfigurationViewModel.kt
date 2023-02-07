package com.simprints.face.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.LicenseVendor
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
) : ViewModel() {
    val configurationState: MutableLiveData<LiveDataEventWithContent<ConfigurationState>> = MutableLiveData()

    fun retrieveLicense(projectId: String, deviceId: String) = viewModelScope.launch {
        licenseRepository.getLicenseStates(projectId, deviceId, LicenseVendor.RANK_ONE_FACE)
            .map { it.toConfigurationState() }
            .collect { configurationState.send(it) }
    }

    fun deleteInvalidLicense() {
        viewModelScope.launch {
            Simber.d("License is invalid, deleting it")
            licenseRepository.deleteCachedLicense()
        }
    }

    private fun LicenseState.toConfigurationState(): ConfigurationState =
        when (this) {
            LicenseState.Started -> ConfigurationState.Started
            LicenseState.Downloading -> ConfigurationState.Downloading
            is LicenseState.FinishedWithSuccess -> ConfigurationState.FinishedWithSuccess(license)
            is LicenseState.FinishedWithError -> ConfigurationState.FinishedWithError(errorCode)
            is LicenseState.FinishedWithBackendMaintenanceError -> ConfigurationState.FinishedWithBackendMaintenanceError(estimatedOutage)
        }
}
