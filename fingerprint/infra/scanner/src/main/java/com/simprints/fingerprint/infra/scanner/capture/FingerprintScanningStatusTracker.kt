package com.simprints.fingerprint.infra.scanner.capture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simprints.core.livedata.LiveDataEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FingerprintScanningStatusTracker @Inject constructor() {
    private val _scanCompleted = MutableLiveData<LiveDataEvent>()
    val scanCompleted: LiveData<LiveDataEvent> get() = _scanCompleted

    fun notifyScanCompleted() {
        _scanCompleted.postValue(LiveDataEvent())
    }
}
