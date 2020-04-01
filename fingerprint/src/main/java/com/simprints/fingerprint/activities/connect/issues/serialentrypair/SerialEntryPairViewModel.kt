package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager

class SerialEntryPairViewModel(
    private val scannerPairingManager: ScannerPairingManager
) : ViewModel() {

    val awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()

    fun startPairing(serialNumber: String) {
        val macAddress = scannerPairingManager.convertSerialNumberToAddress(serialNumber)
        scannerPairingManager.pairOnlyToDevice(macAddress)
        awaitingToPairToMacAddress.postValue(LiveDataEventWithContent(macAddress))
    }
}
