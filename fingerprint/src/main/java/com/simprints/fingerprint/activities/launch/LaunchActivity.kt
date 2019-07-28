package com.simprints.fingerprint.activities.launch

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
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.activities.launch.result.LaunchTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForLaunchActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.extensions.Vibrate.vibrate
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import com.simprints.id.Application
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity(), LaunchContract.View {

    override lateinit var viewPresenter: LaunchContract.Presenter
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        logActivityCreated()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val component = FingerprintComponentBuilder.getComponent(this.application as Application)
        component.inject(this)

        val launchRequest = this.intent.extras?.getParcelable(LaunchTaskRequest.BUNDLE_KEY) as LaunchTaskRequest?
            ?: throw InvalidRequestForLaunchActivityException()

        setButtonClickListeners()
        setClickListenerToPrivacyNotice()

        viewPresenter = LaunchPresenter(component, this, launchRequest)
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
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                    viewPresenter.onActivityResult()
                    viewPresenter.tryAgainFromErrorOrRefusal()
                }
            }
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
        super.onDestroy()
        logActivityDestroyed()
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

    override fun isCurrentTabParental(): Boolean = tabHost.currentTab == 1

    override fun doVibrate() = vibrate(this)

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
