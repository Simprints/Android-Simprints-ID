package com.simprints.fingerprint.activities.connect.issues.nfcpair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.R
import com.simprints.fingerprint.scanner.ScannerPairingManager
import com.simprints.fingerprint.tools.nfc.ComponentMifareUltralight
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag
import java.io.IOException

class NfcPairViewModel(
    private val nfcAdapter: ComponentNfcAdapter,
    private val scannerPairingManager: ScannerPairingManager
) : ViewModel() {

    val awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()
    val showToastWithStringRes = MutableLiveData<LiveDataEventWithContent<Int>>()

    fun handleNfcTagDetected(tag: ComponentNfcTag?) {
        try {
            val mifare = nfcAdapter.getMifareUltralight(tag)
            val macAddress = mifare?.readScannerMacAddress()
                ?: throw IllegalArgumentException("Empty tag")
            startPairing(macAddress)
        } catch (e: IOException) {
            showToastWithStringRes.postValue(LiveDataEventWithContent( R.string.nfc_pair_toast_try_again))
        } catch (e: IllegalArgumentException) {
            showToastWithStringRes.postValue(LiveDataEventWithContent(R.string.nfc_pair_toast_invalid))
        }
    }

    fun startPairing(macAddress: String) {
        scannerPairingManager.pairOnlyToDevice(macAddress)
        awaitingToPairToMacAddress.postValue(LiveDataEventWithContent(macAddress))
    }

    private fun ComponentMifareUltralight.readScannerMacAddress(): String = use { mifare ->
        mifare.connect()
        // Expect an NFC tag with type application/vnd.bluetooth.ep.oob
        // The scanner address is found 15 * 4 = 60 bytes into the payload of the loaded data
        val payload = mifare.readPages(15)
        scannerPairingManager.interpretNfcDataAsScannerMacAddress(payload)
    }
}
