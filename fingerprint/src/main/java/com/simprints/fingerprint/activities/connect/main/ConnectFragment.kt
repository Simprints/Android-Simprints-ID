package com.simprints.fingerprint.activities.connect.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class ConnectFragment(@LayoutRes private val layout: Int) : FingerprintFragment() {

    protected val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()

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
        with(ConnectScannerMainFragmentDirections) {
            val action = when (issue) {
                ConnectScannerIssue.BluetoothOff -> actionConnectScannerMainFragmentToBluetoothOffFragment()
                ConnectScannerIssue.NfcOff -> actionConnectScannerMainFragmentToNfcOffFragment()
                ConnectScannerIssue.NfcPair -> actionConnectScannerMainFragmentToNfcPairFragment()
                ConnectScannerIssue.SerialEntryPair -> actionConnectScannerMainFragmentToSerialEntryPairFragment()
                ConnectScannerIssue.ScannerOff -> actionConnectScannerMainFragmentToScannerOffFragment()
                is ConnectScannerIssue.Ota -> actionConnectScannerMainFragmentToOtaFragment(issue.otaFragmentRequest)
                is ConnectScannerIssue.OtaRecovery, ConnectScannerIssue.OtaFailed -> null
            }
            action?.let { findNavController().navigate(it) }
        }
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
                requireContext(), scannerId,
                onYes = { connectScannerViewModel.handleScannerDisconnectedYesClick() },
                onNo = { connectScannerViewModel.handleScannerDisconnectedNoClick() }
            )
}
