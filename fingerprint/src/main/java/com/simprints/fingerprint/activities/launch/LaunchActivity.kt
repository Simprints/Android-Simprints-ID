package com.simprints.fingerprint.activities.launch

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.LONG_CONSENT_ACTIVITY_REQUEST_CODE
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.REFUSAL_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.InternalConstants.ResultIntents.Companion.ALERT_TRY_AGAIN_RESULT
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.requests.FingerprintRequest
import com.simprints.fingerprint.di.FingerprintsComponentBuilder
import com.simprints.fingerprint.moduleapi.AppAdapter.fromModuleApiToDomainRequest
import com.simprints.fingerprint.tools.extensions.launchAlert
import com.simprints.id.Application
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.ALERT_ACTIVITY_REQUEST
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.Vibrate.vibrate
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity(), LaunchContract.View {

    override lateinit var viewPresenter: LaunchContract.Presenter
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec

    private lateinit var fingerprintRequest: FingerprintRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val iFingerprintRequest: IFingerprintRequest = this.intent.extras?.getParcelable(IFingerprintRequest.BUNDLE_KEY)
            ?: throw IllegalArgumentException("No Request in the bundle") //STOPSHIP
        fingerprintRequest = fromModuleApiToDomainRequest(iFingerprintRequest)

        setButtonClickListeners()
        setClickListenerToPrivacyNotice()

        val component = FingerprintsComponentBuilder.getComponent(this.application as Application)
        viewPresenter = LaunchPresenter(component, this, fingerprintRequest)
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

    override fun setLogoVisibility(visible: Boolean) {
        simprintsLogoWithTagLine.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun handleSetupProgress(progress: Int, detailsId: Int) {
        loadingInfoTextView.visibility = View.VISIBLE
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
            ALERT_TRY_AGAIN_RESULT -> viewPresenter.tryAgainFromErrorScreen()
            else -> viewPresenter.tearDownAppWithResult(resultCode, data)
        }
    }

    override fun requestPermissions(permissions: ArrayList<String>): Observable<Permission> =
        RxPermissions(this).requestEach(*permissions.toTypedArray())

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
        val intent = Intent(this, CollectFingerprintsActivity::class.java)
            .also { it.putExtra(FingerprintRequest.BUNDLE_KEY, fingerprintRequest) }
        startActivityForResult(intent, COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE)
    }

    override fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
    }

    override fun setResultAndFinish(resultCode: Int, resultData: Intent?) {
        setResult(resultCode, resultData)
        finish()
    }

    override fun doLaunchAlert(alert: FingerprintAlert) {
        launchAlert(alert)
    }

    override fun isCurrentTabParental(): Boolean = tabHost.currentTab == 1

    override fun doVibrateIfNecessary(doVibrate: Boolean) = vibrate(this, doVibrate)

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
