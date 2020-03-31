package com.simprints.fingerprint.activities.connect

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import kotlinx.android.synthetic.main.fragment_connect_scanner_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectScannerMainFragment : Fragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_connect_scanner_main, container, false)

    override fun onResume() {
        super.onResume()
        observeLifecycleEvents()
        observeScannerEvents()
    }

    private fun observeLifecycleEvents() {
        connectScannerViewModel.connectScannerIssue.observe(this, LiveDataEventWithContentObserver {
            navigateToScannerIssueFragment(it)
        })
        connectScannerViewModel.scannerConnected.observe(this, LiveDataEventWithContentObserver { success ->
            if (success) {
                connectScannerViewModel.finish.postValue(LiveDataEvent())
            }
        })
    }

    private fun observeScannerEvents() {
        connectScannerViewModel.progress.observe(this, Observer { connectScannerProgressBar.progress = it })
        connectScannerViewModel.message.observe(this, Observer { connectScannerInfoTextView.text = androidResourcesHelper.getString(it) })
        connectScannerViewModel.showScannerErrorDialogWithScannerId.observe(this, LiveDataEventWithContentObserver { showDialogForScannerErrorConfirmation(it) })
    }

    override fun onPause() {
        super.onPause()
        connectScannerViewModel.connectScannerIssue.removeObservers(this)
        connectScannerViewModel.scannerConnected.removeObservers(this)
        connectScannerViewModel.progress.removeObservers(this)
        connectScannerViewModel.message.removeObservers(this)
        connectScannerViewModel.showScannerErrorDialogWithScannerId.removeObservers(this)
    }

    private fun navigateToScannerIssueFragment(issue: ConnectScannerIssue) {
        val action = when (issue) {
            ConnectScannerIssue.BLUETOOTH_OFF -> R.id.action_connectScannerMainFragment_to_bluetoothOffFragment
            ConnectScannerIssue.NFC_OFF -> R.id.action_connectScannerMainFragment_to_nfcOffFragment
            ConnectScannerIssue.NFC_PAIR -> R.id.action_connectScannerMainFragment_to_nfcPairFragment
            ConnectScannerIssue.SERIAL_ENTRY_PAIR -> R.id.action_connectScannerMainFragment_to_serialEntryPairFragment
            ConnectScannerIssue.SCANNER_OFF -> R.id.action_connectScannerMainFragment_to_scannerOffFragment
        }
        findNavController().navigate(action)
    }

    private fun showDialogForScannerErrorConfirmation(scannerId: String) {
        scannerErrorConfirmationDialog = buildConfirmScannerErrorAlertDialog(scannerId).also {
            it.show()
            connectScannerViewModel.logScannerErrorDialogShownToCrashReport()
        }
    }

    private fun buildConfirmScannerErrorAlertDialog(scannerId: String) =
        ConfirmScannerErrorBuilder()
            .build(
                requireContext(), androidResourcesHelper, scannerId,
                onYes = { connectScannerViewModel.handleScannerDisconnectedYesClick() },
                onNo = { connectScannerViewModel.handleScannerDisconnectedNoClick() }
            )
}
