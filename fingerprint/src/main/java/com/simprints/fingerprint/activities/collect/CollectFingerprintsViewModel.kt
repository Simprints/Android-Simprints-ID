package com.simprints.fingerprint.activities.collect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.fingerprint.activities.collect.old.models.DefaultScanConfig
import com.simprints.fingerprint.activities.collect.old.models.Finger
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.scanner.ScannerManager

class CollectFingerprintsViewModel(
    private val scannerManager: ScannerManager
) : ViewModel() {

    lateinit var fingerprintsToCapture: List<FingerIdentifier>

    val activeFingers = MutableLiveData<List<Finger>>()

    val vibrate = LiveDataEvent()
    val moveToNextFinger = LiveDataEvent()

    fun start(fingerprintsToCapture: List<FingerIdentifier>) {
        this.fingerprintsToCapture = fingerprintsToCapture
        setStartingState()
    }

    private fun setStartingState() {
        activeFingers.value = fingerprintsToCapture.map {
            Finger(it, true, DefaultScanConfig().getPriority(it), DefaultScanConfig().getOrder(it))
        }
    }

    fun handleScanButtonPressed() {
        // TODO
    }

    fun handleMissingFingerButtonPressed() {
        // TODO
    }
}
