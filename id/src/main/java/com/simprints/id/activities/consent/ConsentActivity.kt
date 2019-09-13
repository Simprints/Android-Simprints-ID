package com.simprints.id.activities.consent

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.exitform.CoreExitFormActivity
import com.simprints.id.activities.exitform.result.CoreExitFormResult
import com.simprints.id.activities.exitform.result.CoreExitFormResult.Companion.BUNDLE_KEY
import com.simprints.id.activities.exitform.result.CoreExitFormResult.Companion.RESULT_CODE_SUBMIT
import com.simprints.id.activities.longConsent.PricvacyNoticeActivity
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest.Companion.CONSENT_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.response.ConsentResponse
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreResponseCode
import com.simprints.id.tools.TimeHelper
import kotlinx.android.synthetic.main.activity_consent.*
import javax.inject.Inject

class ConsentActivity : AppCompatActivity() {

    private lateinit var viewModel: ConsentViewModel
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec
    private lateinit var askConsentRequestReceived: AskConsentRequest

    @Inject lateinit var viewModelFactory: ConsentViewModelFactory
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var preferencesManager: PreferencesManager

    private var startConsentEventTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        injectDependencies()

        startConsentEventTime = timeHelper.now()

        askConsentRequestReceived = intent.extras?.getParcelable(CONSENT_STEP_BUNDLE) ?: throw InvalidAppRequest()

        viewModel = ViewModelProviders.of(this, viewModelFactory.apply { askConsentRequest = askConsentRequestReceived })
            .get(ConsentViewModel::class.java)

        setupTabs()
        setupObserversForUi()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun setupTabs() {
        tabHost.setup()

        generalConsentTab = tabHost.newTabSpec(GENERAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_general_title))
            .setContent(R.id.generalConsentTextView)

        parentalConsentTab = tabHost.newTabSpec(PARENTAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_parental_title))
            .setContent(R.id.parentalConsentTextView)

        tabHost.addTab(generalConsentTab)

        generalConsentTextView.movementMethod = ScrollingMovementMethod()
        parentalConsentTextView.movementMethod = ScrollingMovementMethod()
    }

    private fun setupObserversForUi() {
        observeGeneralConsentData()
        observeParentalConsentData()
        observeParentalConsentExistence()
    }

    private fun observeGeneralConsentData() {
        viewModel.generalConsentText.observe(this, Observer {
            generalConsentTextView.text = it
        })
    }

    private fun observeParentalConsentData() {
        viewModel.parentalConsentText.observe(this, Observer {
            parentalConsentTextView.text = it
        })
    }

    private fun observeParentalConsentExistence() {
        viewModel.parentalConsentExists.observe(this, Observer {
            if (it) {
                tabHost.addTab(parentalConsentTab)
            }
        })
    }

    fun handleConsentAcceptClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(ConsentEvent.Result.ACCEPTED))
        setResult(CoreResponseCode.CONSENT.value, Intent().apply {
            putExtra(CONSENT_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        })
        finish()
    }

    fun handleConsentDeclineClick(@Suppress("UNUSED_PARAMETER")view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(ConsentEvent.Result.DECLINED))
        if (preferencesManager.modalities.size > 1) {
            startCoreExitFormActivity()
        }
    }

    fun handlePrivacyNoticeClick(@Suppress("UNUSED_PARAMETER")view: View) {
        startPrivacyNoticeActivity()
    }

    private fun buildConsentEventForResult(consentResult: ConsentEvent.Result) =
        ConsentEvent(startConsentEventTime, timeHelper.now(), getCurrentConsentTab(), consentResult)

    private fun getCurrentConsentTab() = when(tabHost.currentTabTag) {
        GENERAL_CONSENT_TAB_TAG -> INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> PARENTAL
        else -> throw Exception()
    }

    private fun startPrivacyNoticeActivity() {
        startActivity(Intent(this, PricvacyNoticeActivity::class.java))
    }

    private fun startCoreExitFormActivity() {
        startActivityForResult(Intent(this, CoreExitFormActivity::class.java), CoreRequestCode.EXIT_FORM.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CODE_SUBMIT) {
            setResult(CoreResponseCode.EXIT_FORM.value, buildExitFormResponse(data))
            finish()
        }
    }

    private fun buildExitFormResponse(data: Intent?) = Intent().apply {
        data?.getParcelableExtra<CoreExitFormResult>(BUNDLE_KEY)?.let {
            putExtra(CONSENT_STEP_BUNDLE, CoreExitFormResponse(it.answer.reason, it.answer.optionalText))
        }
    }

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
