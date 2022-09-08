package com.simprints.clientapi.activities.commcare

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.ClientApiComponent
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.commcare.CommCareAction.Companion.buildCommCareAction
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.id.Application
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import javax.inject.Inject

class CommCareActivity : RequestActivity(), CommCareContract.View {

    companion object {
        private const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"

        // Based on the documentation, we are supposed to send either odk_intent_bundle (for key-values result)
        // or odk_intent_data (for a single integer or string), but apparently due to a bug in commcare
        // if we send `odk_intent_bundle` only, the result is processed correctly, but a toast shows an
        // error message. That is because commcare can't find odk_intent_data
        private const val COMMCARE_DATA_KEY = "odk_intent_data"

        private const val BIOMETRICS_COMPLETE_CHECK_KEY = "biometricsComplete"
        private const val REGISTRATION_GUID_KEY = "guid"
        private const val VERIFICATION_CONFIDENCE_KEY = "confidence"
        private const val VERIFICATION_TIER_KEY = "tier"
        private const val VERIFICATION_GUID_KEY = "guid"
        private const val EXIT_REASON = "exitReason"
        private const val EXIT_EXTRA = "exitExtra"
        private const val SIMPRINTS_SESSION_ID = "sessionId"
        private const val SIMPRINTS_EVENTS = "events"
        private const val SIMPRINTS_SUBJECT_ACTIONS = "subjectActions"
    }

    private val action: CommCareAction
        get() = buildCommCareAction(intent.action)

    @Inject
    lateinit var presenterFactory: ClientApiComponent.CommCarePresenterFactory

    override val presenter: CommCareContract.Presenter by lazy {
        presenterFactory.create(this, action)
    }

    override val guidSelectionNotifier = CommCareGuidSelectionNotifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        ClientApiComponent.getComponent(applicationContext as Application).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun returnRegistration(
        guid: String,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?,
        subjectActions: String?
    ) = Intent().let {
        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(SIMPRINTS_SESSION_ID, sessionId)
            putString(REGISTRATION_GUID_KEY, guid)
            eventsJson?.let { putString(SIMPRINTS_EVENTS, eventsJson) }
            subjectActions?.let { putString(SIMPRINTS_SUBJECT_ACTIONS, subjectActions) }
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    /**
     * CommCare expect Identification result in LibSimprints 1.0.12 format.
     * That's why it is being returned in a different way from others (not inside [COMMCARE_BUNDLE_KEY]).
     */
    override fun returnIdentification(
        identifications: ArrayList<Identification>,
        sessionId: String,
        eventsJson: String?
    ) = Intent().let { intent ->
        intent.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        eventsJson?.let { intent.putExtra(SIMPRINTS_EVENTS, eventsJson) }

        sendOkResult(intent)
    }

    override fun returnVerification(
        confidence: Int,
        tier: Tier,
        guid: String,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?
    ) = Intent().let {
        val data = Bundle().apply {
            putString(SIMPRINTS_SESSION_ID, sessionId)
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(VERIFICATION_CONFIDENCE_KEY, confidence.toString())
            putString(VERIFICATION_TIER_KEY, tier.name)
            putString(VERIFICATION_GUID_KEY, guid)
            eventsJson?.let { putString(SIMPRINTS_EVENTS, eventsJson) }
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnExitForms(
        reason: String,
        extra: String,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?
    ) = Intent().let {
        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(EXIT_REASON, reason)
            putString(SIMPRINTS_SESSION_ID, sessionId)
            putString(EXIT_EXTRA, extra)
            eventsJson?.let { putString(SIMPRINTS_EVENTS, eventsJson) }
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnConfirmation(
        flowCompletedCheck: Boolean,
        sessionId: String,
        eventsJson: String?
    ) =
        Intent().let {
            val data = Bundle().apply {
                putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
                putString(SIMPRINTS_SESSION_ID, sessionId)
                eventsJson?.let { putString(SIMPRINTS_EVENTS, eventsJson) }
            }

            injectDataAsCommCareBundleIntoIntent(it, data)
            sendOkResult(it)
        }

    /**
     * Not being used because CommCare might use CoSync. Next method does the same but with a nullable eventsJson.
     */
    override fun returnErrorToClient(
        errorResponse: ErrorResponse,
        flowCompletedCheck: Boolean,
        sessionId: String
    ) {
        throw InvalidStateForIntentAction("Use the overloaded version with eventsJson")
    }

    override fun returnErrorToClient(
        errorResponse: ErrorResponse,
        flowCompletedCheck: Boolean,
        sessionId: String,
        eventsJson: String?
    ) = Intent().let {
        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(SIMPRINTS_SESSION_ID, sessionId)
            eventsJson?.let { putString(SIMPRINTS_EVENTS, eventsJson) }
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    private fun injectDataAsCommCareBundleIntoIntent(intent: Intent, data: Bundle) {
        intent.putExtra(COMMCARE_BUNDLE_KEY, data)
        intent.putExtra(COMMCARE_DATA_KEY, "")
    }

    override fun injectSessionIdIntoIntent(sessionId: String) {
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
    }
}


