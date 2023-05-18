package com.simprints.id.activities.consent

import android.app.Activity
import android.content.Intent
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.exitform.ShowExitFormWrapper
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.databinding.ActivityConsentBinding
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.orchestrator.steps.core.CoreResponseCode
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.response.AskConsentResponse
import com.simprints.id.orchestrator.steps.core.response.ConsentResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.events.event.domain.models.ConsentEvent
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Result.DECLINED
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload.Type
import com.simprints.infra.resources.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConsentActivity : BaseSplitActivity() {

    private val viewModel: ConsentViewModel by viewModels()
    private val binding by viewBinding(ActivityConsentBinding::inflate)

    private lateinit var askConsentRequestReceived: AskConsentRequest

    @Inject
    lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var exitFormHelper: ExitFormHelper

    private val showRefusal = registerForActivityResult(ShowExitFormWrapper()) { result ->
        exitFormHelper.buildExitFormResponse(result)?.let {
            viewModel.deleteLocationInfoFromSession()
            setResultAndFinish(it)
        }
    }

    private var startConsentEventTime: Long = 0
    private var consentConfiguration: ConsentConfiguration = ConsentConfiguration(
        programName = "",
        organizationName = "",
        collectConsent = false,
        displaySimprintsLogo = false,
        allowParentalConsent = false,
        generalPrompt = null,
        parentalPrompt = null
    )
    private var modalities: List<GeneralConfiguration.Modality> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startConsentEventTime = timeHelper.now()

        askConsentRequestReceived = intent.extras?.getParcelable(CORE_STEP_BUNDLE)
            ?: throw InvalidAppRequest()

        fetchData()
    }

    private fun fetchData() {
        viewModel.configuration.observe(this) {
            consentConfiguration = it.consent
            modalities = it.general.modalities
            showLogoIfNecessary()
            setupTextInUi()
            setupTabs()
        }
    }

    private fun showLogoIfNecessary() {
        binding.simprintsLogoWithTagLine.isVisible = consentConfiguration.displaySimprintsLogo
    }

    private fun setupTextInUi() {
        with(binding) {
            consentDeclineButton.text = getString(R.string.launch_consent_decline_button)
            consentAcceptButton.text = getString(R.string.launch_consent_accept_button)
            privacyNoticeText.text = getString(R.string.privacy_notice_text)
            privacyNoticeText.paintFlags = privacyNoticeText.paintFlags or UNDERLINE_TEXT_FLAG
        }
    }

    private fun buildGeneralConsentText(
        prompt: ConsentConfiguration.ConsentPromptConfiguration,
        programName: String,
        organizationName: String,
        modalities: List<GeneralConfiguration.Modality>,
    ) =
        GeneralConsentTextHelper(prompt, programName, organizationName, modalities).assembleText(
            askConsentRequestReceived,
            this
        )

    private fun buildParentalConsentText(
        prompt: ConsentConfiguration.ConsentPromptConfiguration,
        programName: String,
        organizationName: String,
        modalities: List<GeneralConfiguration.Modality>,
    ) =
        ParentalConsentTextHelper(prompt, programName, organizationName, modalities).assembleText(
            askConsentRequestReceived,
            this
        )

    private fun setupTabs() {
        val generalConsentText = buildGeneralConsentText(
            consentConfiguration.generalPrompt!!,
            consentConfiguration.programName,
            consentConfiguration.organizationName,
            modalities,
        )

        binding.consentTextHolderView.text = generalConsentText

        if (!consentConfiguration.allowParentalConsent && binding.tabHost.tabCount >= 2) { // The tab may already be removed.
            binding.tabHost.removeTabAt(1)
        }

        binding.tabHost.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == GENERAL_CONSENT_TAB_TAG)
                    binding.consentTextHolderView.text = generalConsentText
                else if (tab.position == PARENTAL_CONSENT_TAB_TAG)
                    binding.consentTextHolderView.text = buildParentalConsentText(
                        consentConfiguration.parentalPrompt!!,
                        consentConfiguration.programName,
                        consentConfiguration.organizationName,
                        modalities,
                    )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // We do not need to change the text when a tab is unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // We do not need to refresh the text when a tab is reselected
            }
        })

        binding.consentTextHolderView.movementMethod = ScrollingMovementMethod()
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

    private fun getCurrentConsentTab() = when (binding.tabHost.selectedTabPosition) {
        GENERAL_CONSENT_TAB_TAG -> Type.INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> Type.PARENTAL
        else -> throw IllegalStateException("Invalid consent tab selected")
    }

    private fun startPrivacyNoticeActivity() {
        startActivity(Intent(this, PrivacyNoticeActivity::class.java))
    }

    private fun startExitFormActivity() {
        showRefusal.launch(exitFormHelper.getExitFormFromModalities(modalities))
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
        const val GENERAL_CONSENT_TAB_TAG = 0
        const val PARENTAL_CONSENT_TAB_TAG = 1
    }
}
