package com.simprints.feature.setup.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DeviceID
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.Vendor
import com.simprints.infra.license.Vendor.Companion.NEC
import com.simprints.infra.license.Vendor.Companion.RANK_ONE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class SetupViewModel @Inject constructor(
    private val locationStore: LocationStore,
    private val configManager: ConfigManager,
    private val licenseRepository: LicenseRepository,
    @DeviceID private val deviceID: String,
    private val authStore: AuthStore,
    private val saveLicenseCheckEvent: SaveLicenseCheckEventUseCase
) : ViewModel() {

    val requestLocationPermission: LiveData<Unit>
        get() = _requestLocationPermission
    private val _requestLocationPermission = MutableLiveData<Unit>()

    private val _downloadLicenseState = MutableLiveData<LicenseState>()
    val downloadLicenseState: LiveData<LicenseState>
        get() = _downloadLicenseState

    private val _overallSetupResult = MutableLiveData<Boolean>()
    val overallSetupResult: LiveData<Boolean>
        get() = _overallSetupResult

    private lateinit var requiredLicenses: List<Vendor>

    val requestNotificationPermission: LiveData<Unit>
        get() = _requestNotificationPermission
    private val _requestNotificationPermission = MutableLiveData<Unit>()

    fun start() {
        viewModelScope.launch {
            if (shouldCollectLocation()) {
                // request location permissions
                _requestLocationPermission.postValue(Unit)
            } else {
                // proceed to requesting notification permission right away
                _requestNotificationPermission.postValue(Unit)
            }
        }
    }

    fun downloadRequiredLicenses() {
        viewModelScope.launch {
            requiredLicenses = configManager.getProjectConfiguration().requiredLicenses
            // if there are no required licenses, then the setup is complete
            if (requiredLicenses.isEmpty()) {
                _overallSetupResult.postValue(true)
                return@launch
            }
            requiredLicenses.forEach { licenseVendor ->
                licenseRepository.getLicenseStates(
                    authStore.signedInProjectId,
                    deviceID,
                    licenseVendor
                )
                    .collect { licenceState ->
                        _downloadLicenseState.postValue(licenceState)
                        if (licenceState is LicenseState.FinishedWithError
                            || licenceState is LicenseState.FinishedWithBackendMaintenanceError
                        ) {
                            // Save the license state event
                            saveLicenseCheckEvent(licenseVendor,LicenseStatus.MISSING)
                            _overallSetupResult.postValue(false)
                        }
                        // if this is the last license to download, then update the overall setup result
                        if (licenseVendor == requiredLicenses.last() &&
                            licenceState is LicenseState.FinishedWithSuccess
                        ) {
                            _overallSetupResult.postValue(true)
                        }
                    }
            }
        }
    }

    fun requestNotificationsPermission() {
        _requestNotificationPermission.postValue(Unit)
    }

    fun collectLocation() {
        locationStore.collectLocationInBackground()
    }

    private suspend fun shouldCollectLocation() =
        configManager.getProjectConfiguration().general.collectLocation


}

private val ProjectConfiguration.requiredLicenses: List<Vendor>
    get() = general.modalities.mapNotNull {
        when {
            it == GeneralConfiguration.Modality.FINGERPRINT &&
                fingerprint?.allowedSDKs?.contains(
                    FingerprintConfiguration.BioSdk.NEC
                ) == true -> NEC

            else -> null
        }
    }
