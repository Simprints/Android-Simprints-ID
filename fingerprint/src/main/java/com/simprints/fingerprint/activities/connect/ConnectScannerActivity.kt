package com.simprints.fingerprint.activities.connect

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate
import org.koin.android.viewmodel.ext.android.viewModel

class ConnectScannerActivity : FingerprintActivity() {

    private val viewModel: ConnectScannerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        findNavController(R.id.nav_host_fragment).setGraph(R.navigation.connect_scanner_nav_graph, intent.extras)

        viewModel.launchAlert.activityObserveEventWith { launchAlert(this, it) }
        viewModel.finish.activityObserveEventWith { vibrateAndContinueToNextActivity() }
        viewModel.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> viewModel.retryConnect()
            }
        }
    }

    private fun vibrateAndContinueToNextActivity() {
        Vibrate.vibrate(this)
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
