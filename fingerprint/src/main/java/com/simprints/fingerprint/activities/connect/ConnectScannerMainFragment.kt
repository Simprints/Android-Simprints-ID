package com.simprints.fingerprint.activities.connect

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.tools.Vibrate.vibrate
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
        connectScannerViewModel.connectScannerIssue.observe(this, Observer {
            it?.let {
                // Set to null so it is cleared when we return to this fragment
                connectScannerViewModel.connectScannerIssue.value = null
                navigateToScannerIssueFragment(it)
            }
        })
        connectScannerViewModel.scannerConnected.observe(this, Observer {
            if (it == true) {
                connectScannerViewModel.finish.postValue(Unit)
            }
        })
    }

    private fun observeScannerEvents() {
        connectScannerViewModel.progress.observe(this, Observer { connectScannerProgressBar.progress = it })
        connectScannerViewModel.message.observe(this, Observer { connectScannerInfoTextView.text = androidResourcesHelper.getString(it) })
        connectScannerViewModel.vibrate.observe(this, Observer { it?.let { vibrate(context!!) } })
        connectScannerViewModel.showScannerErrorDialogWithScannerId.observe(this, Observer { it?.let { showDialogForScannerErrorConfirmation(it) } })
    }

    override fun onPause() {
        super.onPause()
        connectScannerViewModel.connectScannerIssue.removeObservers(this)
        connectScannerViewModel.scannerConnected.removeObservers(this)
        connectScannerViewModel.progress.removeObservers(this)
        connectScannerViewModel.message.removeObservers(this)
        connectScannerViewModel.vibrate.removeObservers(this)
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
                context!!, androidResourcesHelper, scannerId,
                onYes = { connectScannerViewModel.handleScannerDisconnectedYesClick() },
                onNo = { connectScannerViewModel.handleScannerDisconnectedNoClick() }
            )
}
