package com.simprints.fingerprint.activities.launch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.tools.LanguageHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.orchestrator.Orchestrator
import com.simprints.fingerprint.activities.orchestrator.OrchestratorCallback
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.REFUSAL_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.exceptions.unexpected.InvalidRequestForFingerprintException
import com.simprints.fingerprint.tools.extensions.Vibrate.vibrate
import com.simprints.id.Application
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_launch.*
import javax.inject.Inject

class LaunchActivity : AppCompatActivity(), LaunchContract.View, OrchestratorCallback {

    override val context: Context by lazy { this }
    @Inject lateinit var orchestrator: Orchestrator

    override lateinit var viewPresenter: LaunchContract.Presenter
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec

    private lateinit var fingerprintRequest: FingerprintRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val component = FingerprintComponentBuilder.getComponent(this.application as Application)
        component.inject(this)

        fingerprintRequest = this.intent.extras?.getParcelable(FingerprintRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForFingerprintException()

        setButtonClickListeners()
        setClickListenerToPrivacyNotice()

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
            startActivity(Intent(this, LongConsentActivity::class.java))
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
        super.onActivityResult(requestCode, resultCode, data)
        orchestrator.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun tryAgain() = viewPresenter.tryAgainFromErrorScreen()
    override fun onActivityResultReceived() { viewPresenter.onActivityResult() }
    override fun resultNotHandleByOrchestrator(resultCode: Int?, data: Intent?) {}
    override fun setResultDataAndFinish(resultCode: Int?, data: Intent?) {
        resultCode?.let {
            setResultAndFinish(it, data)
        }
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
//        val intent = Intent(this, CollectFingerprintsActivity::class.java)
//            .also { it.putExtra(FingerprintRequest.BUNDLE_KEY, fingerprintRequest) }
//        startActivityForResult(intent, COLLECT_FINGERPRINTS_ACTIVITY_REQUEST_CODE)
        setResultAndFinish(0, null)
    }

    override fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
    }

    override fun setResultAndFinish(resultCode: Int, resultData: Intent?) {
        setResult(resultCode, resultData)
        finish()
    }

    override fun doLaunchAlert(fingerprintAlert: FingerprintAlert) {
        launchAlert(this, fingerprintAlert)
    }

    override fun isCurrentTabParental(): Boolean = tabHost.currentTab == 1

    override fun doVibrate() = vibrate(this)

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
