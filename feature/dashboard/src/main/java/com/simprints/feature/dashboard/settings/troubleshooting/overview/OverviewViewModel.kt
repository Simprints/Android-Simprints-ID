package com.simprints.feature.dashboard.settings.troubleshooting.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectIdsUseCase
import com.simprints.feature.dashboard.settings.troubleshooting.overview.usecase.CollectLicenceStatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OverviewViewModel @Inject constructor(
    private val collectIds: CollectIdsUseCase,
    private val collectLicenseStates: CollectLicenceStatesUseCase,
) : ViewModel() {

    val projectIds: LiveData<String>
        get() = _projectIds
    private val _projectIds = MutableLiveData("")

    val licenseStates: LiveData<String>
        get() = _licenseStates
    private val _licenseStates = MutableLiveData("")

    fun collectData() {
        _projectIds.postValue(collectIds())

        viewModelScope.launch {
            _licenseStates.postValue(collectLicenseStates())
        }
    }


}
