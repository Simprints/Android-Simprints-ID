package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.tools.livedata.postEvent

class SerialEntryPairViewModel(
    private val scannerPairingManager: ScannerPairingManager,
    private val serialNumberConverter: SerialNumberConverter
) : ViewModel() {

    val awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()

    fun startPairing(serialNumber: String) {
        val macAddress = serialNumberConverter.convertSerialNumberToMacAddress(serialNumber)
        scannerPairingManager.pairOnlyToDevice(macAddress)
        awaitingToPairToMacAddress.postEvent(macAddress)
    }
}
