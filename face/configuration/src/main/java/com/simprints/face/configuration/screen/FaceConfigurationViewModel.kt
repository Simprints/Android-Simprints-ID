package com.simprints.face.configuration.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.face.configuration.data.FaceConfigurationState
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.Vendor
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FaceConfigurationViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository,
) : ViewModel() {

    private var fetchAttempted = false

    val configurationState: LiveData<LiveDataEventWithContent<FaceConfigurationState>>
        get() = _configurationState
    private val _configurationState = MutableLiveData<LiveDataEventWithContent<FaceConfigurationState>>()

    fun retrieveLicense(projectId: String, deviceId: String) {
        if (fetchAttempted) return

        viewModelScope.launch {
            fetchAttempted = true
            licenseRepository.getLicenseStates(projectId, deviceId, RANK_ONE_FACE_VENDOR)
                .map { it.toConfigurationState() }
                .collect { _configurationState.send(it) }
        }
    }

    fun deleteInvalidLicense() {
        viewModelScope.launch {
            Simber.d("License is invalid, deleting it")
            licenseRepository.deleteCachedLicense()
        }
    }

    private fun LicenseState.toConfigurationState(): FaceConfigurationState =
        when (this) {
            LicenseState.Started -> FaceConfigurationState.Started
            LicenseState.Downloading -> FaceConfigurationState.Downloading
            is LicenseState.FinishedWithSuccess -> FaceConfigurationState.FinishedWithSuccess(license)
            is LicenseState.FinishedWithError -> FaceConfigurationState.FinishedWithError(errorCode)
            is LicenseState.FinishedWithBackendMaintenanceError -> FaceConfigurationState.FinishedWithBackendMaintenanceError(estimatedOutage)
        }

    companion object {
        private val RANK_ONE_FACE_VENDOR = Vendor("RANK_ONE_FACE")
    }
}
