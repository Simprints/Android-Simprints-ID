package com.simprints.feature.troubleshooting.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectLicenceStatesUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectNetworkInformationUseCase
import com.simprints.feature.troubleshooting.overview.usecase.CollectScannerStateUseCase
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase.PingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OverviewViewModel @Inject constructor(
    private val collectIds: CollectIdsUseCase,
    private val collectLicenseStates: CollectLicenceStatesUseCase,
    private val collectNetworkInformation: CollectNetworkInformationUseCase,
    private val doServerPing: PingServerUseCase,
    private val collectScannerState: CollectScannerStateUseCase,
) : ViewModel() {
    val projectIds: LiveData<String>
        get() = _projectIds
    private val _projectIds = MutableLiveData(PLACEHOLDER_TEXT)

    val licenseStates: LiveData<String>
        get() = _licenseStates
    private val _licenseStates = MutableLiveData(PLACEHOLDER_TEXT)

    val networkStates: LiveData<String>
        get() = _networkStates
    private val _networkStates = MutableLiveData(PLACEHOLDER_TEXT)

    val pingResult: LiveData<PingResult>
        get() = _pingResult
    private val _pingResult = MutableLiveData<PingResult>(PingResult.NotDone)

    val scannerState: LiveData<String>
        get() = _scannerState
    private val _scannerState = MutableLiveData("Connecting to scanner...")

    fun collectData() {
        _projectIds.postValue(collectIds())
        viewModelScope.launch { _licenseStates.postValue(collectLicenseStates()) }
        _networkStates.postValue(collectNetworkInformation())
        viewModelScope.launch { _scannerState.postValue(collectScannerState()) }
    }

    fun pingServer() {
        viewModelScope.launch {
            doServerPing().collect { _pingResult.postValue(it) }
        }
    }

    companion object {
        private const val PLACEHOLDER_TEXT = "Collecting data"
    }
}
