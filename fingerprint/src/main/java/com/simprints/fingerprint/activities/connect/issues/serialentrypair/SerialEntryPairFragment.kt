package com.simprints.fingerprint.activities.connect.issues.serialentrypair

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.databinding.FragmentSerialEntryPairBinding
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SerialEntryPairFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val viewModel: SerialEntryPairViewModel by viewModels()
    private val binding by viewBinding(FragmentSerialEntryPairBinding::bind)

    @Inject
    lateinit var scannerPairingManager: ScannerPairingManager

    @Inject
    lateinit var serialNumberConverter: SerialNumberConverter

    @Inject
    lateinit var timeHelper: FingerprintTimeHelper

    @Inject
    lateinit var sessionManager: FingerprintSessionEventsManager

    @Inject
    lateinit var recentUserActivityManager: RecentUserActivityManager

    private lateinit var bluetoothPairStateChangeReceiver: BroadcastReceiver

    // Sometimes the BOND_BONDED state is never sent, so we need to check after a timeout whether the devices are paired
    private val handler = Handler()
    private val determineWhetherPairingWasSuccessful = Runnable {
        checkIfNowBondedToChosenScannerThenProceed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_serial_entry_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(
            AlertScreenEventWithScannerIssue(
                timeHelper.now(),
                ConnectScannerIssue.SerialEntryPair
            )
        )

        binding.serialEntryOkButton.setOnClickListener { parseTextAndCommencePair() }
        setupDoneButtonForEditText()

        viewModel.awaitingToPairToMacAddress.fragmentObserveEventWith {
            binding.serialEntryOkButton.visibility = View.INVISIBLE
            binding.serialEntryPairProgressBar.visibility = View.VISIBLE
            handler.postDelayed(determineWhetherPairingWasSuccessful, PAIRING_WAIT_TIMEOUT)
        }
    }

    private fun setTextInLayout() {
        binding.apply {
            serialEntryOkButton.text = getString(R.string.serial_entry_ok)
            serialEntryPairInstructionsTextView.text = getString(R.string.enter_scanner_number)
            serialEntryPairTitleTextView.text = getString(R.string.serial_entry_pair_title)
            serialEntryPairInstructionsDetailTextView.text =
                getString(R.string.enter_scanner_number_detail)
        }
    }

    private fun setupDoneButtonForEditText() {
        binding.serialEntryEditText.setOnEditorActionListener { _, actionId, _ ->
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
        bluetoothPairStateChangeReceiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
            onPairSuccess = ::checkIfNowBondedToChosenScannerThenProceed,
            onPairFailed = ::handlePairingAttemptFailed
        )
        activity?.registerReceiver(
            bluetoothPairStateChangeReceiver,
            IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    override fun onResume() {
        super.onResume()

        // Show the keyboard as we'll be focused on the serialEntryEditText
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            binding.serialEntryEditText,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
    }

    private fun parseTextAndCommencePair() {
        try {
            val serialNumber = scannerPairingManager.interpretEnteredTextAsSerialNumber(
                binding.serialEntryEditText.text.toString()
            )
            viewModel.startPairing(serialNumber)
        } catch (e: NumberFormatException) {
            context?.showToast(getString(R.string.serial_entry_pair_toast_invalid))
        }
    }

    private fun checkIfNowBondedToChosenScannerThenProceed() {
        lifecycleScope.launch(Dispatchers.Main ) {
            val macAddress = viewModel.awaitingToPairToMacAddress.value?.peekContent()
            if (macAddress != null && scannerPairingManager.isAddressPaired(macAddress)) {
                recentUserActivityManager.updateRecentUserActivity {
                    it.apply {
                        it.lastScannerUsed =
                            serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
                    }
                }
                retryConnectAndFinishFragment()
            } else {
                handlePairingAttemptFailed()
            }
        }
    }

    private fun handlePairingAttemptFailed() {
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
        viewModel.awaitingToPairToMacAddress.value?.let { macAddressEvent ->
            binding.serialEntryPairProgressBar.visibility = View.INVISIBLE
            binding.serialEntryOkButton.visibility = View.VISIBLE
            binding.serialEntryPairInstructionsDetailTextView.visibility = View.INVISIBLE
            binding.serialEntryPairInstructionsTextView.text = String.format(
                getString(R.string.serial_entry_pair_failed),
                serialNumberConverter.convertMacAddressToSerialNumber(macAddressEvent.peekContent())
            )
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
