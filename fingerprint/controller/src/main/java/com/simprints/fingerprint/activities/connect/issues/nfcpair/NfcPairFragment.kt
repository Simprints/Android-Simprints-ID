package com.simprints.fingerprint.activities.connect.issues.nfcpair

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.databinding.FragmentNfcPairBinding
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothDevice
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprint.tools.nfc.ComponentNfcTag
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NfcPairFragment : FingerprintFragment() {

    private val viewModel: NfcPairViewModel by viewModels()
    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val binding by viewBinding(FragmentNfcPairBinding::bind)

    @Inject
    lateinit var nfcManager: NfcManager

    @Inject
    lateinit var scannerPairingManager: ScannerPairingManager

    @Inject
    lateinit var serialNumberConverter: SerialNumberConverter

    @Inject
    lateinit var sessionManager: FingerprintSessionEventsManager

    @Inject
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @Inject
    lateinit var timeHelper: FingerprintTimeHelper

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
        inflater.inflate(R.layout.fragment_nfc_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(
            AlertScreenEventWithScannerIssue(
                timeHelper.now(),
                ConnectScannerIssue.NfcPair
            )
        )

        setupScannerPhoneTappingAnimation()

        binding.couldNotPairTextView.paintFlags =
            binding.couldNotPairTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.couldNotPairTextView.setOnClickListener { goToSerialEntryPair() }

        viewModel.showToastWithStringRes.fragmentObserveEventWith { context?.showToast(getString(it)) }
        viewModel.awaitingToPairToMacAddress.fragmentObserveEventWith { handleAwaitingPair(it) }
    }

    private fun setTextInLayout() {
        binding.couldNotPairTextView.text = getString(R.string.cannot_connect_devices)
        binding.tryAgainButton.text = getString(R.string.try_again_label)
        binding.nfcPairInstructionsTextView.text = getString(R.string.nfc_pair_instructions)
        binding.nfcPairTitleTextView.text = getString(R.string.nfc_pair_title)
    }

    private fun setupScannerPhoneTappingAnimation() {
        AnimationUtils.loadAnimation(requireContext(), R.anim.animation_nfc_pair_scanner)
            .also { scannerAnimation ->
                scannerAnimation.repeatMode = Animation.REVERSE
                scannerAnimation.repeatCount = Animation.INFINITE
                binding.nfcPairScannerImageView.startAnimation(scannerAnimation)
            }
        AnimationUtils.loadAnimation(requireContext(), R.anim.animation_nfc_pair_phone)
            .also { phoneAnimation ->
                phoneAnimation.repeatMode = Animation.REVERSE
                phoneAnimation.repeatCount = Animation.INFINITE
                binding.nfcPairPhoneImageView.startAnimation(phoneAnimation)
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
        super.onDestroy()
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
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
                getString(R.string.nfc_pairing_in_progress),
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            )
        }

        handler.postDelayed(determineWhetherPairingWasSuccessful, PAIRING_WAIT_TIMEOUT)
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
        handler.removeCallbacks(determineWhetherPairingWasSuccessful)
        viewModel.awaitingToPairToMacAddress.value?.let { macAddressEvent ->

            binding.apply {
                couldNotPairTextView.visibility = View.GONE
                nfcPairingProgressBar.visibility = View.INVISIBLE
                tryAgainButton.visibility = View.VISIBLE
                nfcPairInstructionsTextView.text = if (pairingRejected) {
                    getString(R.string.nfc_pairing_rejected_instruction)
                } else {
                    getString(
                        R.string.nfc_pairing_try_again_instruction,
                        serialNumberConverter.convertMacAddressToSerialNumber(macAddressEvent.peekContent())
                    )
                }
                tryAgainButton.setOnClickListener { viewModel.startPairing(macAddressEvent.peekContent()) }
            }
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
        private const val PAIRING_WAIT_TIMEOUT = 6000L
    }
}
