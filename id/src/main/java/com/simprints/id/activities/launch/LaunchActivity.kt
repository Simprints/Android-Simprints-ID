package com.simprints.id.activities.launch

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.TabHost
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.simprints.id.activities.refusal.RefusalActivity
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.InternalConstants.*
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity(), LaunchContract.View {

    override lateinit var viewPresenter: LaunchContract.Presenter
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setButtonClickListeners()
        setClickListenerToPrivacyNotice()

        viewPresenter = LaunchPresenter(this)
        viewPresenter.start()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.handleOnResume()
    }

    private fun setButtonClickListeners() {
        consentDeclineButton.setOnClickListener { viewPresenter.handleDeclinePressed() }
        consentAcceptButton.setOnClickListener { viewPresenter.confirmConsentAndContinueToNextActivity() }
    }

    private fun setClickListenerToPrivacyNotice() {
        privacyNoticeText.setOnClickListener {
            startActivityForResult(Intent(this, LongConsentActivity::class.java), LONG_CONSENT_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun setLanguage(language: String) = LanguageHelper.setLanguage(this, language)

    override fun initTextsInButtons() {
        consentAcceptButton.text = getString(R.string.launch_consent_accept_button)
        consentDeclineButton.text = getString(R.string.launch_consent_decline_button)
    }

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

    override fun initConsentTabs() {
        tabHost.setup()
        generalConsentTab = tabHost.newTabSpec(GENERAL_CONSENT_TAB_TAG).setIndicator(getString(R.string.consent_general_title)).setContent(R.id.generalConsentTextView)
        parentalConsentTab = tabHost.newTabSpec(PARENTAL_CONSENT_TAB_TAG).setIndicator(getString(R.string.consent_parental_title)).setContent(R.id.parentalConsentTextView)

        tabHost.addTab(generalConsentTab)

        generalConsentTextView.movementMethod = ScrollingMovementMethod()
        parentalConsentTextView.movementMethod = ScrollingMovementMethod()
    }

    override fun setTextToGeneralConsent(generalConsentText: String) {
        generalConsentTextView.text = generalConsentText
    }

    override fun addParentalConsentTabWithText(parentalConsentText: String) {
        tabHost.addTab(parentalConsentTab)
        parentalConsentTextView.text = parentalConsentText
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
        viewPresenter.handleOnBackPressed()
    }

    override fun onPause() {
        super.onPause()
        viewPresenter.handleOnPause()
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

    override fun isCurrentTabParental(): Boolean = tabHost.currentTab == 1

    override fun doVibrateIfNecessary(doVibrate: Boolean) = vibrate(this, doVibrate)

    companion object {
        const val COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE = LAST_GLOBAL_REQUEST_CODE + 1
        private const val LONG_CONSENT_ACTIVITY_REQUEST_CODE = LAST_GLOBAL_REQUEST_CODE + 2

        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
