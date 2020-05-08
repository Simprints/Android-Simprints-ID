package com.simprints.clientapi.activities.odk

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkEnrolExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkVerifyExtractor
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
        private const val ODK_EXIT_REASON = "odk-exit-reason"
        private const val ODK_EXIT_EXTRA = "odk-exit-extra"
        private const val CONFIRM_IDENTITY_ACTION = "com.simprints.simodkadapter.CONFIRM_IDENTITY"
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
        ODK_EXIT_EXTRA
    )

    override val presenter: OdkContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier: OdkGuidSelectionNotifier by inject {
        parametersOf(this)
    }

    override val enrollExtractor: EnrollExtractor
        get() = OdkEnrolExtractor(intent, acceptableExtras)

    override val identifyExtractor: IdentifyExtractor
        get() = OdkIdentifyExtractor(intent, acceptableExtras)

    override val verifyExtractor: VerifyExtractor
        get() = OdkVerifyExtractor(intent, acceptableExtras)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != CONFIRM_IDENTITY_ACTION)
            showLauncherScreen()

        loadClientApiKoinModules()
    }

    override fun returnRegistration(registrationId: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnIdentification(idList: String,
                                      confidenceList: String,
                                      tierList: String,
                                      sessionId: String,
                                      flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, idList)
        it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
        it.putExtra(ODK_TIERS_KEY, tierList)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnVerification(id: String, confidence: String, tier: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, id)
        it.putExtra(ODK_CONFIDENCES_KEY, confidence)
        it.putExtra(ODK_TIERS_KEY, tier)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnExitForm(reason: String, extra: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_EXIT_REASON, reason)
        it.putExtra(ODK_EXIT_EXTRA, extra)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    override fun returnConfirmation(flowCompletedCheck: Boolean, sessionId: String) = Intent().let {
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse,
                                     flowCompletedCheck: Boolean,
                                     sessionId: String) = Intent().let {
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
