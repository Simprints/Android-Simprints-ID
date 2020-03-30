package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
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

    // Sometimes the BOND_BONDED state is never sent, so we need to check after a timeout whether the devices are paired
    private val handler = Handler()
    private val determineWhetherPairingWasSuccessful = Runnable {
        checkIfNowBondedToSingleScannerThenProceed()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_serial_entry_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serialEntryOkButton.setOnClickListener { parseTextAndCommencePair() }
        serialEntryEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
                parseTextAndCommencePair()
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
        viewModel.isAwaitingPairSerialNumber.observe(this, Observer {
            it?.let {
                serialEntryOkButton.visibility = View.INVISIBLE
                serialEntryPairProgressBar.visibility = View.VISIBLE
                handler.postDelayed(determineWhetherPairingWasSuccessful, PAIRING_WAIT_TIMEOUT)
            }
        })
        val inputMethodManager: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(serialEntryEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onPause() {
        super.onPause()
        viewModel.toastMessage.removeObservers(this)
        viewModel.isAwaitingPairSerialNumber.removeObservers(this)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    private fun parseTextAndCommencePair() {
        try {
            val serialNumber = scannerPairingManager.interpretEnteredTextAsSerialNumber(serialEntryEditText.text.toString())
            viewModel.startPairing(serialNumber)
        } catch (e: NumberFormatException) {
            context?.showToast(getString(R.string.serial_entry_pair_toast_invalid))
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
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
        viewModel.isAwaitingPairSerialNumber.value?.let { macAddress ->
            serialEntryPairProgressBar.visibility = View.INVISIBLE
            serialEntryOkButton.visibility = View.VISIBLE
            serialEntryPairInstructionsDetailTextView.visibility = View.INVISIBLE
            serialEntryPairInstructionsTextView.text = getString(R.string.serial_entry_pair_failed, scannerPairingManager.convertAddressToSerialNumber(macAddress))
        }
    }

    private fun retryConnectAndFinishFragment() {
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
        connectScannerViewModel.retryConnect()
        findNavController().navigate(R.id.action_serialEntryPairFragment_to_connectScannerMainFragment)
    }

    companion object {
        private const val PAIRING_WAIT_TIMEOUT = 4000L
    }
}
