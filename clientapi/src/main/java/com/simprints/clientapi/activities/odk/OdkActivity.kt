package com.simprints.clientapi.activities.odk

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.odk.OdkAction.*
import com.simprints.clientapi.activities.odk.OdkAction.Companion.buildOdkAction
import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkEnrolExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkVerifyExtractor
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
        private const val ODK_EXIT_REASON = "odk-exit-reason"
        private const val ODK_EXIT_EXTRA = "odk-exit-extra"

        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_REGISTER_BIOMETRICS_COMPLETE = "odk-register-biometrics-complete"

        private const val ODK_IDENTIFY_BIOMETRICS_COMPLETE = "odk-identify-biometrics-complete"
        private const val ODK_MATCH_CONFIDENCE_KEY = "odk-confidence"

        private const val ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE = "odk-confirm-identity-biometrics-complete"

        private const val ODK_VERIFY_BIOMETRICS_COMPLETE = "odk-verify-biometrics-complete"
    }

    //For some reason, Survey CTO sends the callback field in the callout Intent.
    //Because SID doesn't expect these fields, the intent is marked as suspicious.
    //Added these fields as "acceptable", so a Suspicious event is not generated.
    private val acceptableExtras = listOf(
        ODK_REGISTRATION_ID_KEY,
        ODK_GUIDS_KEY,
        ODK_BIOMETRICS_COMPLETE_CHECK_KEY,
        ODK_CONFIDENCES_KEY,
        ODK_TIERS_KEY,
        ODK_SESSION_ID,
        ODK_EXIT_REASON,
        ODK_EXIT_EXTRA,
        ODK_REGISTRATION_ID_KEY,
        ODK_REGISTER_BIOMETRICS_COMPLETE,
        ODK_IDENTIFY_BIOMETRICS_COMPLETE,
        ODK_MATCH_CONFIDENCE_KEY,
        ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE,
        ODK_VERIFY_BIOMETRICS_COMPLETE
    )

    private val action: OdkAction
        get() = buildOdkAction(intent.action)

    override val presenter: OdkContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier: OdkGuidSelectionNotifier by inject {
        parametersOf(this)
    }

    override val enrolExtractor: EnrolExtractor
        get() = OdkEnrolExtractor(intent, acceptableExtras)

    override val identifyExtractor: IdentifyExtractor
        get() = OdkIdentifyExtractor(intent, acceptableExtras)

    override val verifyExtractor: VerifyExtractor
        get() = OdkVerifyExtractor(intent, acceptableExtras)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
    }

    override fun returnRegistration(registrationId: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        it.putExtra(ODK_SESSION_ID, sessionId)
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnIdentification(idList: String,
                                      confidenceList: String,
                                      tierList: String,
                                      sessionId: String,
                                      matchConfidence: String,
                                      flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, idList)
        it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
        it.putExtra(ODK_TIERS_KEY, tierList)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_MATCH_CONFIDENCE_KEY, matchConfidence)
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnVerification(id: String, confidence: String, tier: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, id)
        it.putExtra(ODK_CONFIDENCES_KEY, confidence)
        it.putExtra(ODK_TIERS_KEY, tier)
        it.putExtra(ODK_SESSION_ID, sessionId)
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnExitForm(reason: String, extra: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_EXIT_REASON, reason)
        it.putExtra(ODK_EXIT_EXTRA, extra)
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    override fun returnConfirmation(flowCompletedCheck: Boolean, sessionId: String) = Intent().let {
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse,
                                     flowCompletedCheck: Boolean,
                                     sessionId: String) = Intent().let {
        addFlowCompletedCheckBasedOnAction(it, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    private fun addFlowCompletedCheckBasedOnAction(intent: Intent, flowCompletedCheck: Boolean) {
        when (action) {
            OdkActionFollowUpAction.ConfirmIdentity -> intent.putExtra(ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            OdkActionFollowUpAction.EnrolLastBiometrics -> intent.putExtra(ODK_REGISTER_BIOMETRICS_COMPLETE, flowCompletedCheck)
            Enrol -> intent.putExtra(ODK_REGISTER_BIOMETRICS_COMPLETE, flowCompletedCheck)
            Verify -> intent.putExtra(ODK_VERIFY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            Identify -> intent.putExtra(ODK_IDENTIFY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            Invalid -> intent.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
