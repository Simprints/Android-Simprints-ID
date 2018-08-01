package com.simprints.id.activities.launch

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.simprints.id.R
import com.simprints.id.activities.refusal.RefusalActivity
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.InternalConstants.*
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_launch.*


class LaunchActivity : AppCompatActivity(), LaunchContract.View {

    override lateinit var viewPresenter: LaunchContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setButtonClickListeners()

        viewPresenter = LaunchPresenter(this)
        viewPresenter.start()
    }

    private fun setButtonClickListeners() {
        consentDeclineButton.setOnClickListener { viewPresenter.handleOnBackOrDeclinePressed() }
        consentAcceptButton.setOnClickListener { viewPresenter.confirmConsentAndContinueToNextActivity() }
    }

    override fun setLanguage(language: String) = LanguageHelper.setLanguage(this, language)

    override fun handleSetupProgress(progress: Int, detailsId: Int) {
        launchProgressBar.progress = progress
        loadingInfoTextView.setText(detailsId)
    }

    override fun handleSetupFinished() {
        launchProgressBar.progress = 100
        consentDeclineButton.visibility = View.VISIBLE
        consentAcceptButton.visibility = View.VISIBLE
        loadingInfoTextView.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESOLUTION_REQUEST,
            GOOGLE_SERVICE_UPDATE_REQUEST ->
                viewPresenter.updatePositionTracker(requestCode, resultCode, data)
            COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE,
            ALERT_ACTIVITY_REQUEST,
            REFUSAL_ACTIVITY_REQUEST ->
                whenReturningFromAnotherActivity(resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun whenReturningFromAnotherActivity(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_TRY_AGAIN -> viewPresenter.tryAgainFromErrorScreen()
            else -> viewPresenter.tearDownAppWithResult(resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        viewPresenter.handleOnRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        viewPresenter.handleOnBackOrDeclinePressed()
    }

    override fun onDestroy() {
        viewPresenter.handleOnDestroy()
        super.onDestroy()
    }

    override fun continueToNextActivity() {
        startActivityForResult(
            Intent(this@LaunchActivity, CollectFingerprintsActivity::class.java),
            COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE)
        launchLayout.visibility = View.INVISIBLE
    }

    override fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
    }

    override fun setResultAndFinish(resultCode: Int, resultData: Intent?) {
        setResult(resultCode, resultData)
        finish()
    }

    override fun doLaunchAlert(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun doVibrateIfNecessary(doVibrate: Boolean) = vibrate(this, doVibrate)

    companion object {
        const val COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE = LAST_GLOBAL_REQUEST_CODE + 1
    }
}
