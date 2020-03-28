package com.simprints.fingerprint.activities.connect

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.connect.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.tools.Vibrate.vibrate
import kotlinx.android.synthetic.main.fragment_connect_scanner_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ConnectScannerMainFragment : Fragment() {

    private val viewModel: ConnectScannerViewModel by viewModel()
    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_connect_scanner_main, container, false)

    override fun onResume() {
        super.onResume()
        observeScannerEvents()
    }

    private fun observeScannerEvents() {
        viewModel.progress.observe(this, Observer { connectScannerProgressBar.progress = it })
        viewModel.message.observe(this, Observer { connectScannerInfoTextView.text = androidResourcesHelper.getString(it) })
        viewModel.vibrate.observe(this, Observer { it?.let { vibrate(context!!) } })
        viewModel.showScannerErrorDialogWithScannerId.observe(this, Observer { it?.let { showDialogForScannerErrorConfirmation(it) } })
    }

    override fun onPause() {
        super.onPause()
        viewModel.progress.removeObservers(this)
        viewModel.message.removeObservers(this)
        viewModel.vibrate.removeObservers(this)
        viewModel.showScannerErrorDialogWithScannerId.removeObservers(this)
    }

    private fun showDialogForScannerErrorConfirmation(scannerId: String) {
        scannerErrorConfirmationDialog = buildConfirmScannerErrorAlertDialog(scannerId).also {
            it.show()
            viewModel.logScannerErrorDialogShownToCrashReport()
        }
    }

    private fun buildConfirmScannerErrorAlertDialog(scannerId: String) =
        ConfirmScannerErrorBuilder()
            .build(
                context!!, androidResourcesHelper, scannerId,
                onYes = { viewModel.handleScannerDisconnectedYesClick() },
                onNo = { viewModel.handleScannerDisconnectedNoClick() }
            )
}
