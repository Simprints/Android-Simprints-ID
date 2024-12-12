package com.simprints.fingerprint.connect.screens.issues.serialentrypair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class SerialEntryPairViewModel @Inject constructor(
    private val scannerPairingManager: ScannerPairingManager,
    private val serialNumberConverter: SerialNumberConverter,
) : ViewModel() {
    var scannerNumber: String? = null
    val awaitingToPairToMacAddress: LiveData<LiveDataEventWithContent<String>>
        get() = _awaitingToPairToMacAddress
    private val _awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()

    fun startPairing(serialNumber: String) {
        val macAddress = serialNumberConverter.convertSerialNumberToMacAddress(serialNumber)

        scannerPairingManager.startPairingToDevice(macAddress)
        _awaitingToPairToMacAddress.send(macAddress)
    }
}
