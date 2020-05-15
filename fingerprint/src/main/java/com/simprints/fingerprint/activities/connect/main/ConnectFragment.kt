package com.simprints.fingerprint.activities.connect.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class ConnectFragment(@LayoutRes private val layout: Int) : FingerprintFragment() {

    protected val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    protected val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiComponents()
        observeScannerEvents()
        observeLifecycleEvents()
    }

    protected abstract fun initUiComponents()

    protected abstract fun observeScannerEvents()

    private fun observeLifecycleEvents() {
        connectScannerViewModel.connectScannerIssue.fragmentObserveEventWith {
            navigateToScannerIssueFragment(it)
        }
        connectScannerViewModel.scannerConnected.fragmentObserveEventWith { success ->
            if (success) {
                connectScannerViewModel.finishConnectActivity()
            }
        }
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

    protected fun showDialogForScannerErrorConfirmation(scannerId: String) {
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
