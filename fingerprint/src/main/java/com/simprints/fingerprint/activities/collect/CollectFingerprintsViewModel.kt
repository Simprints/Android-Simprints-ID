package com.simprints.fingerprint.activities.collect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.fingerprint.activities.collect.domain.Finger
import com.simprints.fingerprint.activities.collect.old.models.FingerScanConfig
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.scanner.ScannerManager

class CollectFingerprintsViewModel(
    private val scannerManager: ScannerManager
) : ViewModel() {

    private lateinit var fingerprintsToCapture: List<FingerIdentifier>

    val state = MutableLiveData<CollectFingerprintsState>()
    fun state() = state.value ?: TODO("Oops")

    val vibrate = LiveDataEvent()
    val moveToNextFinger = LiveDataEvent()

    private fun updateState(block: CollectFingerprintsState.() -> Unit) {
        state.postValue(state.value?.apply { block() })
    }

    fun start(fingerprintsToCapture: List<FingerIdentifier>) {
        this.fingerprintsToCapture = fingerprintsToCapture
        setStartingState()
    }

    private fun setStartingState() {
        state.value = CollectFingerprintsState(fingerprintsToCapture.map {
            Finger(it, FingerScanConfig.DEFAULT.getPriority(it), FingerScanConfig.DEFAULT.getOrder(it))
        }.associateWith { FingerCollectionState.NotCollected })
    }

    fun updateSelectedFingerIfNotBusy(index: Int) {
        updateState { currentFingerIndex = index }
    }

    fun handleScanButtonPressed() {
        // TODO
    }

    fun handleMissingFingerButtonPressed() {
        // TODO
    }
}
