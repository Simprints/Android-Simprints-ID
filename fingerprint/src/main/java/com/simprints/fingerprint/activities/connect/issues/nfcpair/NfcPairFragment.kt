package com.simprints.fingerprint.activities.connect.issues.nfcpair

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
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

    private val bluetoothPairStateChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                when (intent.getIntExtra(ComponentBluetoothDevice.EXTRA_BOND_STATE, ComponentBluetoothDevice.BOND_NONE)) {
                    ComponentBluetoothDevice.BOND_BONDED -> checkIfNowBondedToSingleScannerThenProceed()
                }
            }
        }
    }

    private val nfcAdapter: ComponentNfcAdapter by inject()
    private val scannerPairingManager: ScannerPairingManager by inject()

    private val viewModel: NfcPairViewModel by viewModel()
    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nfc_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.registerReceiver(bluetoothPairStateChangeReceiver, IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED))

        couldNotPairTextView.paintFlags = couldNotPairTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        couldNotPairTextView.setOnClickListener { goToSerialEntryPair() }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            requireActivity(),
            ::handleNfcTagDetected,
            ComponentNfcAdapter.FLAG_READER_NFC_A or ComponentNfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        viewModel.toastMessage.observe(this, Observer { it?.let { context?.showToast(it) } })
        viewModel.isAwaitingPairScannerSerialNumber.observe(this, Observer {
            it?.let { handleAwaitingPair(it) }
        })
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(requireActivity())
        viewModel.toastMessage.removeObservers(this)
        viewModel.isAwaitingPairScannerSerialNumber.removeObservers(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    private fun handleNfcTagDetected(tag: ComponentNfcTag?) {
        Vibrate.vibrate(requireContext())
        viewModel.handleNfcTagDetected(tag)
    }

    private fun handleAwaitingPair(serialNumber: String) {
        couldNotPairTextView.visibility = View.INVISIBLE
        nfcPairingProgressBar.visibility = View.VISIBLE
        nfcPairInstructionsTextView.text = getString(R.string.nfc_pairing_in_progress, serialNumber)
    }

    private fun goToSerialEntryPair() {
        findNavController().navigate(R.id.action_nfcPairFragment_to_serialEntryPairFragment)
    }

    private fun checkIfNowBondedToSingleScannerThenProceed() {
        if (scannerPairingManager.isOnlyPairedToOneScanner()) {
            retryConnectAndFinishFragment()
        }
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.retryConnect()
        findNavController().navigate(R.id.action_nfcPairFragment_to_connectScannerMainFragment)
    }
}
