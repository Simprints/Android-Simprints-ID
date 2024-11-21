package com.simprints.feature.dashboard.settings.troubleshooting.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.PingServerUseCase.PingResult
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectLicenceStatesUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectNetworkInformationUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.PingServerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class OverviewViewModel @Inject constructor(
    private val collectIds: CollectIdsUseCase,
    private val collectLicenseStates: CollectLicenceStatesUseCase,
    private val collectNetworkInformation: CollectNetworkInformationUseCase,
    private val doServerPing: PingServerUseCase,
) : ViewModel() {

    val projectIds: LiveData<String>
        get() = _projectIds
    private val _projectIds = MutableLiveData("")

    val licenseStates: LiveData<String>
        get() = _licenseStates
    private val _licenseStates = MutableLiveData("")

    val networkStates: LiveData<String>
        get() = _networkStates
    private val _networkStates = MutableLiveData("")

    val pingResult: LiveData<PingResult>
        get() = _pingResult
    private val _pingResult = MutableLiveData<PingResult>(PingResult.NotDone)

    fun collectData() {
        _projectIds.postValue(collectIds())
        viewModelScope.launch { _licenseStates.postValue(collectLicenseStates()) }
        _networkStates.postValue(collectNetworkInformation())
    }

    fun pingServer() {
        viewModelScope.launch {
            doServerPing().collect { _pingResult.postValue(it) }
        }
    }
}
