package com.simprints.fingerprint.connect.screens.issues.serialentrypair

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hideKeyboard
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentSerialEntryPairBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class SerialEntryPairFragment : Fragment(R.layout.fragment_serial_entry_pair) {
    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val viewModel: SerialEntryPairViewModel by viewModels()
    private val binding by viewBinding(FragmentSerialEntryPairBinding::bind)

    @Inject
    lateinit var scannerPairingManager: ScannerPairingManager

    @Inject
    lateinit var serialNumberConverter: SerialNumberConverter

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    @Inject
    lateinit var recentUserActivityManager: RecentUserActivityManager

    private lateinit var bluetoothPairStateChangeReceiver: BroadcastReceiver

    // Sometimes the BOND_BONDED state is never sent, so we need to check after a timeout whether the devices are paired
    private var determineWhetherPairingWasSuccessfulJob: Job? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        screenReporter.reportSerialEntry()

        binding.serialEntryOkButton.setOnClickListener { parseTextAndCommencePair() }
        viewModel.scannerNumber?.let { binding.serialEntryEditText.setText(it) }

        setupDoneButtonForEditText()

        binding.serialEntryEditText.addTextChangedListener(
            afterTextChanged = { text ->
                viewModel.scannerNumber = text?.toString()
            },
        )

        viewModel.awaitingToPairToMacAddress.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                binding.serialEntryOkButton.visibility = View.INVISIBLE
                binding.serialEntryPairProgressBar.visibility = View.VISIBLE

                determineWhetherPairingWasSuccessfulJob = lifecycleScope.launch {
                    delay(PAIRING_WAIT_TIMEOUT)
                    checkIfNowBondedToChosenScannerThenProceed()
                }
            },
        )
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
            onPairFailed = ::handlePairingAttemptFailed,
        )
        activity?.registerReceiver(
            bluetoothPairStateChangeReceiver,
            IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED),
        )
    }

    override fun onResume() {
        super.onResume()

        // Show the keyboard as we'll be focused on the serialEntryEditText
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            binding.serialEntryEditText,
            InputMethodManager.SHOW_IMPLICIT,
        )
    }

    override fun onPause() {
        activity?.hideKeyboard()
        super.onPause()
    }

    override fun onStop() {
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        super.onDestroy()
    }

    private fun parseTextAndCommencePair() {
        try {
            val serialNumber = scannerPairingManager.interpretEnteredTextAsSerialNumber(
                viewModel.scannerNumber.orEmpty(),
            )
            viewModel.startPairing(serialNumber)
        } catch (_: NumberFormatException) {
            context?.showToast(IDR.string.fingerprint_connect_serial_entry_pair_toast_invalid)
        }
    }

    private fun checkIfNowBondedToChosenScannerThenProceed() {
        lifecycleScope.launch(Dispatchers.Main) {
            val macAddress = viewModel.awaitingToPairToMacAddress.value?.peekContent()
            if (macAddress != null && scannerPairingManager.isAddressPaired(macAddress)) {
                recentUserActivityManager.updateRecentUserActivity {
                    it.copy(lastScannerUsed = serialNumberConverter.convertMacAddressToSerialNumber(macAddress))
                }
                finishFragmentAndRetryConnect()
            } else {
                handlePairingAttemptFailed(false)
            }
        }
    }

    private fun handlePairingAttemptFailed(pairingRejected: Boolean) {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        if (isAdded && view != null) {
            viewModel.awaitingToPairToMacAddress.value?.let { macAddressEvent ->
                binding.serialEntryPairProgressBar.visibility = View.INVISIBLE
                binding.serialEntryOkButton.visibility = View.VISIBLE
                binding.serialEntryPairInstructionsDetailTextView.visibility = View.INVISIBLE
                binding.serialEntryPairInstructionsTextView.text = if (pairingRejected) {
                    getString(IDR.string.fingerprint_connect_serial_entry_pair_rejected)
                } else {
                    getString(
                        IDR.string.fingerprint_connect_serial_entry_pair_failed,
                        serialNumberConverter.convertMacAddressToSerialNumber(macAddressEvent.peekContent()),
                    )
                }
            }
        }
    }

    private fun finishFragmentAndRetryConnect() {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        // Order of execution is important here. It's necessary to navigate first and attempt to
        // reconnect afterwards.
        findNavController().navigateSafely(
            this,
            SerialEntryPairFragmentDirections.actionSerialEntryPairFragmentToConnectProgressFragment(),
            navOptions { popUpTo(R.id.connectProgressFragment) },
        )
        connectScannerViewModel.connect()
    }

    companion object {
        private const val PAIRING_WAIT_TIMEOUT = 4000L
    }
}
