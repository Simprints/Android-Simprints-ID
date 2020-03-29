package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.scanner.ScannerPairingManager

class SerialEntryPairViewModel(
    private val scannerPairingManager: ScannerPairingManager
) : ViewModel() {

    val isAwaitingPairSerialNumber = MutableLiveData<String?>(null)
    val toastMessage = MutableLiveData<String?>(null)

    fun startPairing(serialNumber: String) {
        val macAddress = scannerPairingManager.convertSerialNumberToAddress(serialNumber)
        val couldStartPairing = scannerPairingManager.pairOnlyToDevice(macAddress)
        if (couldStartPairing) {
            isAwaitingPairSerialNumber.postValue(macAddress)
        } else {
            toastMessage.postValue("Could not pair to device. Please pair manually.")
        }
    }
}
