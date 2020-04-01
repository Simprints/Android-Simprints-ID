package com.simprints.fingerprint.activities.connect.issues.nfcpair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.R
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag
import java.io.IOException

class NfcPairViewModel(
    private val nfcManager: NfcManager,
    private val scannerPairingManager: ScannerPairingManager
) : ViewModel() {

    val awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()
    val showToastWithStringRes = MutableLiveData<LiveDataEventWithContent<Int>>()

    fun handleNfcTagDetected(tag: ComponentNfcTag) {
        try {
            val macAddress = nfcManager.readMacAddressDataFromBluetoothEasyPairTag(tag)
            if (!scannerPairingManager.isScannerAddress(macAddress)) {
                throw IllegalArgumentException("NFC chip does not contain a valid Simprints scanner MAC address")
            }
            startPairing(macAddress)
        } catch (e: IOException) {
            showToastWithStringRes.postEvent(R.string.nfc_pair_toast_try_again)
        } catch (e: IllegalArgumentException) {
            showToastWithStringRes.postEvent(R.string.nfc_pair_toast_invalid)
        }
    }

    fun startPairing(macAddress: String) {
        scannerPairingManager.pairOnlyToDevice(macAddress)
        awaitingToPairToMacAddress.postEvent(macAddress)
    }
}
