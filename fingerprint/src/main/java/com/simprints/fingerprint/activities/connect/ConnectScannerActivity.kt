package com.simprints.fingerprint.activities.connect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForConnectScannerActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import org.koin.android.viewmodel.ext.android.viewModel

class ConnectScannerActivity : FingerprintActivity() {

    private val viewModel: ConnectScannerViewModel by viewModel()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val connectScannerRequest: ConnectScannerTaskRequest =
            this.intent.extras?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()

        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        observeLifecycleEvents()
    }

    private fun observeLifecycleEvents() {
        viewModel.connectScannerIssue.observe(this, Observer { it?.let { navigateToScannerIssueFragment(it) } })
        viewModel.launchAlert.observe(this, Observer { it?.let { launchAlert(this, it) } })
        viewModel.finish.observe(this, Observer { it?.let { continueToNextActivity() } })
    }

    private fun navigateToScannerIssueFragment(issue: ConnectScannerIssue) {
        val action = when (issue) {
            ConnectScannerIssue.BLUETOOTH_OFF -> R.id.action_connectScannerMainFragment_to_bluetoothOffFragment
            ConnectScannerIssue.NFC_OFF -> R.id.action_connectScannerMainFragment_to_nfcOffFragment
            ConnectScannerIssue.NFC_PAIR -> R.id.action_connectScannerMainFragment_to_nfcPairFragment
            ConnectScannerIssue.SERIAL_ENTRY_PAIR -> R.id.action_connectScannerMainFragment_to_serialEntryFragment
            ConnectScannerIssue.TURN_ON_SCANNER -> R.id.action_connectScannerMainFragment_to_turnOnScannerFragment
        }
        findNavController(R.id.nav_host_fragment).navigate(action)
    }

    override fun onPause() {
        super.onPause()
        viewModel.connectScannerIssue.removeObservers(this)
        viewModel.launchAlert.removeObservers(this)
        viewModel.finish.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                    scannerErrorConfirmationDialog?.dismiss()
                    viewModel.retryConnect()
                }
            }
        }
    }

    private fun continueToNextActivity() {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())
        })
    }

    private fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun onBackPressed() {
        goToRefusalActivity()
    }
}
