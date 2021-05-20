package com.simprints.id.activities.consent

import android.app.Activity
import android.content.Intent
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TabHost
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.domain.modality.Modality
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.eventsystem.event.domain.models.ConsentEvent
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload.Result.DECLINED
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload.Type
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.databinding.ActivityConsentBinding
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreResponseCode
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.response.AskConsentResponse
import com.simprints.id.orchestrator.steps.core.response.ConsentResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.tools.LocationManager
import javax.inject.Inject

class ConsentActivity : BaseSplitActivity() {

    private lateinit var viewModel: ConsentViewModel
    private val binding by viewBinding(ActivityConsentBinding::inflate)

    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec
    private lateinit var askConsentRequestReceived: AskConsentRequest

    @Inject lateinit var viewModelFactory: ConsentViewModelFactory
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var preferencesManager: IdPreferencesManager
    @Inject lateinit var exitFormHelper: ExitFormHelper
    @Inject lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var jsonHelper: JsonHelper

    private var startConsentEventTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        injectDependencies()

        startConsentEventTime = timeHelper.now()

        askConsentRequestReceived = intent.extras?.getParcelable(CORE_STEP_BUNDLE)
            ?: throw InvalidAppRequest()

        viewModel = ViewModelProvider(this, viewModelFactory).get(ConsentViewModel::class.java)

        showLogoIfNecessary()
        setupTextInUi()
        setupTabs()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun showLogoIfNecessary() {
        binding.simprintsLogoWithTagLine.isVisible = preferencesManager.logoExists
    }

    private fun setupTextInUi() {
        with(binding) {
            consentDeclineButton.text = getString(R.string.launch_consent_decline_button)
            consentAcceptButton.text = getString(R.string.launch_consent_accept_button)
            privacyNoticeText.text = getString(R.string.privacy_notice_text)
            privacyNoticeText.paintFlags = privacyNoticeText.paintFlags or UNDERLINE_TEXT_FLAG
        }

        with(preferencesManager) {
            binding.generalConsentTextView.text =
                buildGeneralConsentText(generalConsentOptionsJson, programName, organizationName, modalities, jsonHelper)
            binding.parentalConsentTextView.text =
                buildParentalConsentText(parentalConsentOptionsJson, programName, organizationName, modalities, jsonHelper)
        }

    }

    private fun buildGeneralConsentText(generalConsentOptionsJson: String,
                                        programName: String,
                                        organizationName: String,
                                        modalities: List<Modality>,
                                        jsonHelper: JsonHelper) =
        GeneralConsentTextHelper(
            generalConsentOptionsJson,
            programName, organizationName, modalities,
            crashReportManager,
            jsonHelper
        ).assembleText(askConsentRequestReceived, this)

    private fun buildParentalConsentText(parentalConsentOptionsJson: String,
                                         programName: String,
                                         organizationName: String,
                                         modalities: List<Modality>,
                                         jsonHelper: JsonHelper) =
        ParentalConsentTextHelper(
            parentalConsentOptionsJson,
            programName, organizationName, modalities,
            crashReportManager,
            jsonHelper
        ).assembleText(askConsentRequestReceived, this)

    private fun setupTabs() {
        binding.tabHost.setup()

        generalConsentTab = binding.tabHost.newTabSpec(GENERAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_general_title))
            .setContent(R.id.generalConsentTextView)

        parentalConsentTab = binding.tabHost.newTabSpec(PARENTAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_parental_title))
            .setContent(R.id.parentalConsentTextView)

        binding.tabHost.addTab(generalConsentTab)
        if (preferencesManager.parentalConsentExists) {
            binding.tabHost.addTab(parentalConsentTab)
        }

        binding.generalConsentTextView.movementMethod = ScrollingMovementMethod()
        binding.parentalConsentTextView.movementMethod = ScrollingMovementMethod()
    }

    fun handleConsentAcceptClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(ACCEPTED))
        val consentIntent = Intent().apply {
            putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        }
        setResult(CoreResponseCode.CONSENT.value, consentIntent)
        finish()
    }

    fun handleConsentDeclineClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(DECLINED))
        startExitFormActivity()
    }

    fun handlePrivacyNoticeClick(@Suppress("UNUSED_PARAMETER") view: View) {
        startPrivacyNoticeActivity()
    }

    private fun buildConsentEventForResult(consentResult: ConsentPayload.Result) =
        ConsentEvent(startConsentEventTime, timeHelper.now(), getCurrentConsentTab(), consentResult)

    private fun getCurrentConsentTab() = when (binding.tabHost.currentTabTag) {
        GENERAL_CONSENT_TAB_TAG -> Type.INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> Type.PARENTAL
        else -> throw IllegalStateException("Invalid consent tab selected")
    }

    private fun startPrivacyNoticeActivity() {
        startActivity(Intent(this, PrivacyNoticeActivity::class.java))
    }

    private fun startExitFormActivity() {
        val exitFormActivityClass =
            exitFormHelper.getExitFormActivityClassFromModalities(preferencesManager.modalities)

        exitFormActivityClass?.let {
            startActivityForResult(
                Intent().setClassName(this, it),
                CoreRequestCode.EXIT_FORM.value
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        exitFormHelper.buildExitFormResponseForCore(data)?.let {
            deleteLocationInfoFromSession()
            setResultAndFinish(it)
        }
    }

    private fun deleteLocationInfoFromSession() {
        inBackground {
            val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()
            currentSessionEvent.payload.location = null
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, coreResponse)
    }

    override fun onBackPressed() {
        startExitFormActivity()
    }

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
