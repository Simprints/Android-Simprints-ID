package com.simprints.fingerprint.connect.screens.issues.nfcpair

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentNfcPairBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcTag
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.system.Vibrate
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class NfcPairFragment : Fragment(R.layout.fragment_nfc_pair) {
    private val binding by viewBinding(FragmentNfcPairBinding::bind)
    private val viewModel: NfcPairViewModel by viewModels()
    private val connectViewModel: ConnectScannerViewModel by activityViewModels()

    @Inject
    lateinit var nfcManager: NfcManager

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
        screenReporter.reportNfcPairing()

        setupScannerPhoneTappingAnimation()

        binding.couldNotPairTextView.paintFlags = binding.couldNotPairTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.couldNotPairTextView.setOnClickListener { goToSerialEntryPair() }

        viewModel.showToastWithStringRes.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                requireContext().showToast(it)
            },
        )
        viewModel.awaitingToPairToMacAddress.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                handleAwaitingPair(it)
            },
        )
    }

    private fun setupScannerPhoneTappingAnimation() {
        AnimationUtils.loadAnimation(requireContext(), R.anim.animation_nfc_pair_scanner).also { scannerAnimation ->
            scannerAnimation.repeatMode = Animation.REVERSE
            scannerAnimation.repeatCount = Animation.INFINITE
            binding.nfcPairScannerImageView.startAnimation(scannerAnimation)
        }
        AnimationUtils.loadAnimation(requireContext(), R.anim.animation_nfc_pair_phone).also { phoneAnimation ->
            phoneAnimation.repeatMode = Animation.REVERSE
            phoneAnimation.repeatCount = Animation.INFINITE
            binding.nfcPairPhoneImageView.startAnimation(phoneAnimation)
        }
    }

    override fun onStart() {
        super.onStart()
        bluetoothPairStateChangeReceiver = scannerPairingManager.bluetoothPairStateChangeReceiver(
            onPairSuccess = ::checkIfNowBondedToChosenScannerThenProceed,
            onPairFailed = ::handlePairingAttemptFailed,
        )
        requireActivity().registerReceiver(
            bluetoothPairStateChangeReceiver,
            IntentFilter(ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED),
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            nfcManager.enableReaderMode(requireActivity())
            nfcManager.channelTags.consumeEach { handleNfcTagDetected(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableReaderMode(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothPairStateChangeReceiver)
    }

    override fun onDestroy() {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        super.onDestroy()
    }

    private fun handleNfcTagDetected(tag: ComponentNfcTag) {
        Vibrate.vibrate(requireContext())
        viewModel.handleNfcTagDetected(tag)
    }

    private fun handleAwaitingPair(macAddress: String) {
        binding.apply {
            tryAgainButton.visibility = View.INVISIBLE
            couldNotPairTextView.visibility = View.GONE
            nfcPairingProgressBar.visibility = View.VISIBLE
            nfcPairInstructionsTextView.text = String.format(
                getString(IDR.string.fingerprint_connect_nfc_pairing_in_progress),
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress),
            )
        }

        determineWhetherPairingWasSuccessfulJob = lifecycleScope.launch {
            delay(PAIRING_WAIT_TIMEOUT)
            checkIfNowBondedToChosenScannerThenProceed()
        }
    }

    private fun checkIfNowBondedToChosenScannerThenProceed() {
        lifecycleScope.launch(Dispatchers.Main) {
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
                handlePairingAttemptFailed(false)
            }
        }
    }

    private fun handlePairingAttemptFailed(pairingRejected: Boolean) {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        viewModel.awaitingToPairToMacAddress.value?.let { macAddressEvent ->

            binding.apply {
                couldNotPairTextView.visibility = View.GONE
                nfcPairingProgressBar.visibility = View.INVISIBLE
                tryAgainButton.visibility = View.VISIBLE
                nfcPairInstructionsTextView.text = if (pairingRejected) {
                    getString(IDR.string.fingerprint_connect_nfc_pairing_rejected_instruction)
                } else {
                    getString(
                        IDR.string.fingerprint_connect_nfc_pairing_try_again_instruction,
                        serialNumberConverter.convertMacAddressToSerialNumber(macAddressEvent.peekContent()),
                    )
                }
                tryAgainButton.setOnClickListener { viewModel.startPairing(macAddressEvent.peekContent()) }
            }
        }
    }

    private fun retryConnectAndFinishFragment() {
        determineWhetherPairingWasSuccessfulJob?.cancel()
        connectViewModel.connect()
    }

    private fun goToSerialEntryPair() {
        findNavController().navigateSafely(this, NfcPairFragmentDirections.actionNfcPairFragmentToSerialEntryPairFragment())
    }

    companion object {
        private const val PAIRING_WAIT_TIMEOUT = 6000L
    }
}
