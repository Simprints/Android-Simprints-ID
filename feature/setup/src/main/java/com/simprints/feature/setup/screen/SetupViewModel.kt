package com.simprints.feature.setup.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val locationStore: LocationStore,
    private val configManager: ConfigManager
) : ViewModel() {

    val requestLocationPermission: LiveData<Unit>
        get() = _requestLocationPermission
    private val _requestLocationPermission = MutableLiveData<Unit>()

    val finish: LiveData<Boolean>
        get() = _finish
    private val _finish = MutableLiveData<Boolean>()


    fun collectLocation() {
        locationStore.collectLocationInBackground()
    }

    fun start() {
        viewModelScope.launch {
            if (shouldCollectLocation()) {
                // request location permissions
                _requestLocationPermission.postValue(Unit)
            } else {
                _finish.postValue(true)
            }
        }
    }

    private suspend fun shouldCollectLocation() =
        configManager.getProjectConfiguration().general.collectLocation
}
