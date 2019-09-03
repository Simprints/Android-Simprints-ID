package com.simprints.fingerprint.activities.launch

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.simprints.core.tools.LanguageHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.launch.confirmscannererror.ConfirmScannerErrorBuilder
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForLaunchActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate.vibrate
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import kotlinx.android.synthetic.main.activity_launch.*
import org.koin.android.ext.android.inject

class LaunchActivity : AppCompatActivity() {

    private lateinit var launchRequest: LaunchTaskRequest
    private val viewModel: LaunchViewModel by inject()

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        acquireFingerprintKoinModules()
        logActivityCreated()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        launchRequest = this.intent.extras?.getParcelable(LaunchTaskRequest.BUNDLE_KEY) as LaunchTaskRequest?
            ?: throw InvalidRequestForLaunchActivityException()

        LanguageHelper.setLanguage(this, launchRequest.language)

        viewModel.progress.observe(this, Observer { launchProgressBar.progress = it })
        viewModel.message.observe(this, Observer { loadingInfoTextView.setText(it) })
        viewModel.vibrate.observe(this, Observer { vibrate(this) })

        viewModel.launchRefusal.observe(this, Observer { goToRefusalActivity() })
        viewModel.launchAlert.observe(this, Observer { launchAlert(this, it) })
        viewModel.finish.observe(this, Observer { continueToNextActivity() })

        viewModel.showScannerErrorDialogWithScannerId.observe(this, Observer { showDialogForScannerErrorConfirmation(it) })

        viewModel.start()
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
                    viewModel.tryAgainFromErrorOrRefusal()
                }
            }
        }
    }

    private fun continueToNextActivity() {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(LaunchTaskResult.BUNDLE_KEY, LaunchTaskResult())
        })
    }

    private fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
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
                this, scannerId,
                onYes = { viewModel.handleScannerDisconnectedYesClick() },
                onNo = { viewModel.handleScannerDisconnectedNoClick() }
            )

    override fun onBackPressed() {
        viewModel.handleOnBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        logActivityDestroyed()
        releaseFingerprintKoinModules()
    }
}
