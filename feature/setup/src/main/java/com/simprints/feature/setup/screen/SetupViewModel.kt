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
internal class SetupViewModel @Inject constructor(
    private val locationStore: LocationStore,
    private val configManager: ConfigManager,
) : ViewModel() {

    val requestLocationPermission: LiveData<Unit>
        get() = _requestLocationPermission
    private val _requestLocationPermission = MutableLiveData<Unit>()

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

    fun requestNotificationsPermission() {
        _requestNotificationPermission.postValue(Unit)
    }

    fun collectLocation() {
        locationStore.collectLocationInBackground()
    }

    private suspend fun shouldCollectLocation() =
        configManager.getProjectConfiguration().general.collectLocation
}
