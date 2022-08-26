package com.simprints.fingerprint.activities.connect.issues.scanneroff

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.core.R as CR
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.databinding.FragmentScannerOffBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ScannerOffFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentScannerOffBinding::bind)
    private val timeHelper: FingerprintTimeHelper by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_scanner_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.ScannerOff))

        initTryAgainButton()
        initCouldNotConnectTextView()
        initViewModelObservation()
        initRetryConnectionBehaviour()
    }

    private fun setTextInLayout() {
        binding.tryAgainButton.text = getString(R.string.try_again_label)
        binding.scannerOffInstructionsTextView.text = getString(R.string.scanner_off_instructions)
        binding.scannerOffTitleTextView.text = getString(R.string.scanner_off_title)
    }

    private fun initTryAgainButton() {
        binding.tryAgainButton.setOnClickListener {
            startRetryingToConnect()
        }
    }

    private fun initCouldNotConnectTextView() {
        connectScannerViewModel.showScannerErrorDialogWithScannerId.value?.let { scannerIdEvent ->
            binding.couldNotConnectTextView.paintFlags = binding.couldNotConnectTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.couldNotConnectTextView.text = String.format(getString(R.string.not_my_scanner), scannerIdEvent.peekContent())
            binding.couldNotConnectTextView.setOnClickListener { connectScannerViewModel.handleIncorrectScanner() }
            binding.couldNotConnectTextView.visibility = View.VISIBLE
        }
    }

    private fun initViewModelObservation() {
        connectScannerViewModel.isConnecting.observe(viewLifecycleOwner) { isConnecting ->
            if (isConnecting) {
                setupConnectingMode()
            } else {
                setupTryAgainMode()
            }
        }
        connectScannerViewModel.scannerConnected.fragmentObserveEventWith { success ->
            if (success) {
                handleScannerConnectedEvent()
            }
        }
        connectScannerViewModel.connectScannerIssue.fragmentObserveEventWith {
            connectScannerViewModel.stopConnectingAndResetState()
            goToAppropriateIssueFragment(it)
        }
    }

    private fun initRetryConnectionBehaviour() {
        startRetryingToConnect()
    }

    private fun startRetryingToConnect() {
        connectScannerViewModel.startRetryingToConnect()
    }

    private fun handleScannerConnectedEvent() {
        binding.apply {
            scannerOffProgressBar.visibility = View.INVISIBLE
            couldNotConnectTextView.visibility = View.INVISIBLE
            tryAgainButton.visibility = View.VISIBLE
            tryAgainButton.isEnabled = false
            tryAgainButton.text = getString(R.string.scanner_on)
            tryAgainButton.setBackgroundColor(resources.getColor(CR.color.simprints_green, null))
        }

        Handler().postDelayed({ connectScannerViewModel.finishConnectActivity() }, FINISHED_TIME_DELAY_MS)
    }

    private fun setupConnectingMode() {
        binding.apply {
            scannerOffProgressBar.visibility = View.VISIBLE
            tryAgainButton.visibility = View.INVISIBLE
            tryAgainButton.isEnabled = false
        }
    }

    private fun setupTryAgainMode() {
        binding.apply {
            scannerOffProgressBar.visibility = View.INVISIBLE
            tryAgainButton.visibility = View.VISIBLE
            tryAgainButton.isEnabled = true
        }
    }

    private fun goToAppropriateIssueFragment(issue: ConnectScannerIssue) {
        with(ScannerOffFragmentDirections) {
            val navAction = when (issue) {
                ConnectScannerIssue.NfcOff -> actionScannerOffFragmentToNfcOffFragment()
                ConnectScannerIssue.NfcPair -> actionScannerOffFragmentToNfcPairFragment()
                ConnectScannerIssue.SerialEntryPair -> actionScannerOffFragmentToSerialEntryPairFragment()
                ConnectScannerIssue.BluetoothOff -> actionScannerOffFragmentToBluetoothOffFragment()
                is ConnectScannerIssue.Ota -> actionScannerOffFragmentToOtaFragment(issue.otaFragmentRequest)
                ConnectScannerIssue.ScannerOff, is ConnectScannerIssue.OtaRecovery, ConnectScannerIssue.OtaFailed -> null
            }
            navAction?.let { findNavController().navigate(it) }
        }
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
