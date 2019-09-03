package com.simprints.fingerprint.activities.launch

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.tools.LanguageHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.launch.confirmScannerError.ConfirmScannerErrorBuilder
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
import org.koin.core.parameter.parametersOf

class LaunchActivity : AppCompatActivity(), LaunchContract.View {

    private lateinit var launchRequest: LaunchTaskRequest
    override val viewPresenter: LaunchContract.Presenter by inject { parametersOf(this, launchRequest) }

    private var scannerErrorConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        acquireFingerprintKoinModules()
        logActivityCreated()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        launchRequest = this.intent.extras?.getParcelable(LaunchTaskRequest.BUNDLE_KEY) as LaunchTaskRequest?
            ?: throw InvalidRequestForLaunchActivityException()

        viewPresenter.start()
    }

    override fun setLanguage(language: String) = LanguageHelper.setLanguage(this, language)

    override fun handleSetupProgress(progress: Int, @StringRes detailsId: Int) {
        loadingInfoTextView.visibility = View.VISIBLE
        launchProgressBar.progress = progress
        loadingInfoTextView.setText(detailsId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> viewPresenter.tryAgainFromErrorOrRefusal()
            }
        }
    }

    override fun onBackPressed() {
        viewPresenter.handleOnBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        logActivityDestroyed()
        releaseFingerprintKoinModules()
    }

    override fun continueToNextActivity() {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(LaunchTaskResult.BUNDLE_KEY, LaunchTaskResult())
        })
    }

    override fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    override fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun doLaunchAlert(fingerprintAlert: FingerprintAlert) {
        launchAlert(this, fingerprintAlert)
    }

    override fun doVibrate() = vibrate(this)

    override fun showDialogForScannerErrorConfirmation(scannerId: String) {
        scannerErrorConfirmationDialog = buildConfirmScannerErrorAlertDialog(scannerId).also {
            it.show()
            viewPresenter.logScannerErrorDialogShownToCrashReport()
        }
    }

    private fun buildConfirmScannerErrorAlertDialog(scannerId: String) =
        ConfirmScannerErrorBuilder()
            .build(
                this, scannerId,
                onYes = { viewPresenter.handleScannerDisconnectedYesClick() },
                onNo = { viewPresenter.handleScannerDisconnectedNoClick() }
            )

    override fun dismissScannerErrorConfirmationDialog() {
        scannerErrorConfirmationDialog?.dismiss()
    }
}
