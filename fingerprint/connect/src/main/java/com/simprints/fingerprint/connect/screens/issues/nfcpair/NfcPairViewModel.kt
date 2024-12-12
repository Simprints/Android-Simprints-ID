package com.simprints.fingerprint.connect.screens.issues.nfcpair

import android.nfc.TagLostException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcTag
import com.simprints.infra.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
internal class NfcPairViewModel @Inject constructor(
    private val nfcManager: NfcManager,
    private val scannerPairingManager: ScannerPairingManager,
) : ViewModel() {
    val awaitingToPairToMacAddress: LiveData<LiveDataEventWithContent<String>>
        get() = _awaitingToPairToMacAddress
    private val _awaitingToPairToMacAddress = MutableLiveData<LiveDataEventWithContent<String>>()

    val showToastWithStringRes: LiveData<LiveDataEventWithContent<Int>>
        get() = _showToastWithStringRes
    private val _showToastWithStringRes = MutableLiveData<LiveDataEventWithContent<Int>>()

    fun handleNfcTagDetected(tag: ComponentNfcTag) {
        try {
            val macAddress = nfcManager.readMacAddressDataFromBluetoothEasyPairTag(tag)
            if (!scannerPairingManager.isScannerAddress(macAddress)) {
                throw IllegalArgumentException("NFC chip does not contain a valid Simprints scanner MAC address")
            }
            startPairing(macAddress)
        } catch (e: IOException) {
            _showToastWithStringRes.send(R.string.fingerprint_connect_nfc_pair_toast_try_again)
        } catch (e: IllegalArgumentException) {
            _showToastWithStringRes.send(R.string.fingerprint_connect_nfc_pair_toast_invalid)
        } catch (e: SecurityException) {
            _showToastWithStringRes.send(R.string.fingerprint_connect_nfc_pair_toast_try_again)
        } catch (e: TagLostException) {
            _showToastWithStringRes.send(R.string.fingerprint_connect_nfc_pair_toast_try_again)
        }
    }

    fun startPairing(macAddress: String) {
        scannerPairingManager.startPairingToDevice(macAddress)
        _awaitingToPairToMacAddress.send(macAddress)
    }
}
