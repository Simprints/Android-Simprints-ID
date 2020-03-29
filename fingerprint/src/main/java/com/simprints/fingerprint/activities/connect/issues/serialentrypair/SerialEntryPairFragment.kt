package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.scanner.ScannerPairingManager
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import kotlinx.android.synthetic.main.fragment_serial_entry_pair.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SerialEntryPairFragment : Fragment() {

    private val scannerPairingManager: ScannerPairingManager by inject()

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val viewModel: SerialEntryPairViewModel by viewModel()

    private val bluetoothPairStateChangeReceiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
        onPairSuccess = ::checkIfNowBondedToSingleScannerThenProceed,
        onPairFailed = ::handlePairingAttemptFailed
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_serial_entry_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serialEntryOkButton.setOnClickListener { tryPairToScanner() }
        serialEntryEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                tryPairToScanner()
                true
            } else {
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(bluetoothPairStateChangeReceiver, IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED))
    }

    override fun onResume() {
        super.onResume()
        viewModel.toastMessage.observe(this, Observer {
            it?.let {
                viewModel.toastMessage.value = null
                context?.showToast(it)
            }
        })
        viewModel.isAwaitingPairSerialNumber.observe(this, Observer {
            it?.let {
                serialEntryOkButton.visibility = View.INVISIBLE
                serialEntryPairProgressBar.visibility = View.VISIBLE
            }
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.toastMessage.removeObservers(this)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    private fun tryPairToScanner() {
        try {
            val serialNumber = scannerPairingManager.interpretEnteredTextAsSerialNumber(serialEntryEditText.text.toString())
            viewModel.startPairing(serialNumber)
        } catch (e: NumberFormatException) {
            viewModel.toastMessage.postValue("Please enter the 6 digits of the serial number")
        }
    }

    private fun checkIfNowBondedToSingleScannerThenProceed() {
        if (scannerPairingManager.isOnlyPairedToOneScanner()) {
            retryConnectAndFinishFragment()
        } else {
            handlePairingAttemptFailed()
        }
    }

    private fun handlePairingAttemptFailed() {
        viewModel.isAwaitingPairSerialNumber.value?.let { macAddress ->
            serialEntryPairProgressBar.visibility = View.INVISIBLE
            serialEntryOkButton.visibility = View.VISIBLE
            serialEntryPairInstructionsDetailTextView.visibility = View.INVISIBLE
            serialEntryPairInstructionsTextView.text = getString(R.string.serial_entry_pair_failed, scannerPairingManager.convertAddressToSerialNumber(macAddress))
        }
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.retryConnect()
        findNavController().navigate(R.id.action_serialEntryPairFragment_to_connectScannerMainFragment)
    }
}
