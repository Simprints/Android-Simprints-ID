package com.simprints.fingerprint.activities.connect.issues.nfcpair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.scanner.ScannerPairingManager
import com.simprints.fingerprint.tools.nfc.ComponentMifareUltralight
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag
import java.io.IOException

class NfcPairViewModel(
    private val nfcAdapter: ComponentNfcAdapter,
    private val scannerPairingManager: ScannerPairingManager
) : ViewModel() {

    val isAwaitingPairMacAddress = MutableLiveData<String?>(null)
    val toastMessage = MutableLiveData<String?>(null)

    fun handleNfcTagDetected(tag: ComponentNfcTag?) {
        try {
            val mifare = nfcAdapter.getMifareUltralight(tag)
            val macAddress = mifare?.readScannerMacAddress() ?: throw IllegalArgumentException("Empty tag")
            startPairing(macAddress)
        } catch (e: IOException) {
            toastMessage.postValue("Could not read NFC chip, please try again")
        } catch (e: IllegalArgumentException) {
            toastMessage.postValue("Invalid NFC chip detected")
        }
    }

    fun startPairing(macAddress: String) {
        val couldStartPairing = scannerPairingManager.pairOnlyToDevice(macAddress)
        if (couldStartPairing) {
            isAwaitingPairMacAddress.postValue(macAddress)
        } else {
            toastMessage.postValue("Could not pair to device. Please pair manually.")
        }
    }

    private fun ComponentMifareUltralight.readScannerMacAddress(): String = use { mifare ->
        mifare.connect()
        // Expect an NFC tag with type application/vnd.bluetooth.ep.oob
        // The scanner address is found 15 * 4 = 60 bytes into the payload of the loaded data
        val payload = mifare.readPages(15)
        scannerPairingManager.interpretNfcDataAsScannerMacAddress(payload)
    }
}
