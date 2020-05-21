package com.simprints.fingerprint.activities.connect.issues.scanneroff

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import kotlinx.android.synthetic.main.fragment_scanner_off.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ScannerOffFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val resourceHelper: FingerprintAndroidResourcesHelper by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_scanner_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        initTryAgainButton()
        initCouldNotConnectTextView()
        initRetryConnectBehaviour()
    }

    private fun setTextInLayout() {
        with(resourceHelper) {
            tryAgainButton.text = getString(R.string.try_again_label)
            scannerOffInstructionsTextView.text = getString(R.string.scanner_off_instructions)
            scannerOffTitleTextView.text = getString(R.string.scanner_off_title)
        }
    }

    private fun initRetryConnectBehaviour() {
        connectScannerViewModel.retryConnect()
        connectScannerViewModel.scannerConnected.fragmentObserveEventWith { success ->
            when (success) {
                true -> handleScannerConnected()
                false -> connectScannerViewModel.retryConnect()
            }
        }
    }

    private fun initCouldNotConnectTextView() {
        connectScannerViewModel.showScannerErrorDialogWithScannerId.value?.let { scannerIdEvent ->
            couldNotConnectTextView.paintFlags = couldNotConnectTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            couldNotConnectTextView.text = resourceHelper.getString(R.string.not_my_scanner, arrayOf(scannerIdEvent.peekContent()))
            couldNotConnectTextView.setOnClickListener { connectScannerViewModel.handleIncorrectScanner() }
            couldNotConnectTextView.visibility = View.VISIBLE
        }
        connectScannerViewModel.connectScannerIssue.fragmentObserveEventWith {
            connectScannerViewModel.stopConnectingAndResetState()
            goToAppropriateIssueFragment(it)
        }
    }

    // The tryAgainButton doesn't actually do anything - we're already retrying in the background
    // Show a progress bar to make it known that something is happening
    private fun initTryAgainButton() {
        tryAgainButton.setOnClickListener {
            scannerOffProgressBar.visibility = View.VISIBLE
            tryAgainButton.visibility = View.INVISIBLE
            tryAgainButton.isEnabled = false
        }
    }

    private fun handleScannerConnected() {
        scannerOffProgressBar.visibility = View.INVISIBLE
        couldNotConnectTextView.visibility = View.INVISIBLE
        tryAgainButton.visibility = View.VISIBLE
        tryAgainButton.isEnabled = false
        tryAgainButton.text = resourceHelper.getString(R.string.scanner_on)
        tryAgainButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ connectScannerViewModel.finishConnectActivity() }, FINISHED_TIME_DELAY_MS)
    }

    private fun goToAppropriateIssueFragment(issue: ConnectScannerIssue) {
        with(ScannerOffFragmentDirections) {
            val navAction = when (issue) {
                ConnectScannerIssue.NfcOff -> actionScannerOffFragmentToNfcOffFragment()
                ConnectScannerIssue.NfcPair -> actionScannerOffFragmentToNfcPairFragment()
                ConnectScannerIssue.SerialEntryPair -> actionScannerOffFragmentToSerialEntryPairFragment()
                ConnectScannerIssue.BluetoothOff -> actionScannerOffFragmentToBluetoothOffFragment()
                is ConnectScannerIssue.Ota -> actionScannerOffFragmentToOtaFragment(issue.otaFragmentRequest)
                ConnectScannerIssue.ScannerOff -> null
            }
            navAction?.let { findNavController().navigate(it) }
        }
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
