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

    val isAwaitingPairScannerSerialNumber = MutableLiveData<String?>(null)
    val toastMessage = MutableLiveData<String>()

    fun handleNfcTagDetected(tag: ComponentNfcTag?) {
        try {
            val mifare = nfcAdapter.getMifareUltralight(tag)
            val scannerMacAddress = mifare?.readScannerMacAddress() ?: throw IllegalArgumentException("Empty tag")
            val couldStartPairing = scannerPairingManager.pairOnlyToDevice(scannerMacAddress)
            if (couldStartPairing) {
                isAwaitingPairScannerSerialNumber.postValue(scannerMacAddress)
            } else {
                toastMessage.postValue("Could not pair to device. Please pair manually.")
            }
        } catch (e: IOException) {
            toastMessage.postValue("Could not read NFC chip, please try again")
        } catch (e: IllegalArgumentException) {
            toastMessage.postValue("Invalid NFC chip detected")
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
