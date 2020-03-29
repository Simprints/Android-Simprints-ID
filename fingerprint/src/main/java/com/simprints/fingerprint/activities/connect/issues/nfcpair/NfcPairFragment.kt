package com.simprints.fingerprint.activities.connect.issues.nfcpair

import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.scanner.ScannerPairingManager
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import kotlinx.android.synthetic.main.fragment_nfc_pair.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class NfcPairFragment : Fragment() {

    private val nfcAdapter: ComponentNfcAdapter by inject()
    private val scannerPairingManager: ScannerPairingManager by inject()

    private val viewModel: NfcPairViewModel by viewModel()
    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    private val bluetoothPairStateChangeReceiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
        onPairSuccess = ::checkIfNowBondedToSingleScannerThenProceed,
        onPairFailed = ::handlePairingAttemptFailed
    )

    // Sometimes the BOND_BONDED state is never sent, so we need to check after a timeout whether the devices are paired
    private val handler = Handler()
    private val determineWhetherPairingWasSuccessful = ::checkIfNowBondedToSingleScannerThenProceed

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nfc_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        couldNotPairTextView.paintFlags = couldNotPairTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        couldNotPairTextView.setOnClickListener { goToSerialEntryPair() }
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(bluetoothPairStateChangeReceiver, IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED))
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            requireActivity(),
            ::handleNfcTagDetected,
            ComponentNfcAdapter.FLAG_READER_NFC_A or ComponentNfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        viewModel.toastMessage.observe(this, Observer {
            it?.let {
                viewModel.toastMessage.value = null
                context?.showToast(it)
            }
        })
        viewModel.isAwaitingPairMacAddress.observe(this, Observer {
            it?.let { handleAwaitingPair(it) }
        })
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(requireActivity())
        viewModel.toastMessage.removeObservers(this)
        viewModel.isAwaitingPairMacAddress.removeObservers(this)
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    private fun handleNfcTagDetected(tag: ComponentNfcTag?) {
        Vibrate.vibrate(requireContext())
        viewModel.handleNfcTagDetected(tag)
    }

    private fun handleAwaitingPair(macAddress: String) {
        tryAgainButton.visibility = View.INVISIBLE
        couldNotPairTextView.visibility = View.GONE
        nfcPairingProgressBar.visibility = View.VISIBLE
        nfcPairInstructionsTextView.text = getString(R.string.nfc_pairing_in_progress, scannerPairingManager.convertAddressToSerialNumber(macAddress))
        handler.postDelayed(determineWhetherPairingWasSuccessful, PAIRING_WAIT_TIMEOUT)
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
        viewModel.isAwaitingPairMacAddress.value?.let { macAddress ->
            couldNotPairTextView.visibility = View.GONE
            nfcPairingProgressBar.visibility = View.INVISIBLE
            tryAgainButton.visibility = View.VISIBLE
            nfcPairInstructionsTextView.text = getString(R.string.nfc_pairing_try_again_instruction, scannerPairingManager.convertAddressToSerialNumber(macAddress))
            tryAgainButton.setOnClickListener { viewModel.startPairing(macAddress) }
        }
    }

    private fun retryConnectAndFinishFragment() {
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
        connectScannerViewModel.retryConnect()
        findNavController().navigate(R.id.action_nfcPairFragment_to_connectScannerMainFragment)
    }

    private fun goToSerialEntryPair() {
        findNavController().navigate(R.id.action_nfcPairFragment_to_serialEntryPairFragment)
    }

    companion object {
        private const val PAIRING_WAIT_TIMEOUT = 4000L
    }
}
