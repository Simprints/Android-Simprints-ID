package com.simprints.clientapi.activities.libsimprints

import android.content.Intent
import com.simprints.clientapi.ClientApiModule
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Companion.buildLibSimprintsAction
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.exceptions.InvalidStateForIntentAction
import com.simprints.clientapi.identity.DefaultGuidSelectionNotifier
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.tokenization.TokenizationManager
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    private val action: LibSimprintsAction
        get() = buildLibSimprintsAction(intent.action)

    @Inject
    lateinit var libSimprintsPresenterFactory: ClientApiModule.LibSimprintsPresenterFactory

    @Inject
    lateinit var tokenizationManagerParam: TokenizationManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var authStore: AuthStore

    override val tokenizationManager: TokenizationManager by lazy {
        tokenizationManagerParam
    }

    override suspend fun getProject(): Project? =
        runCatching { configManager.getProject(authStore.signedInProjectId) }.getOrNull()

    override val presenter: LibSimprintsContract.Presenter by lazy {
        libSimprintsPresenterFactory.create(this, action)
    }

    override val guidSelectionNotifier = DefaultGuidSelectionNotifier(this)

    override fun returnRegistration(
        registration: Registration,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?,
        subjectActions: String?
    ) = Intent().let { intent ->

        intent.putExtra(Constants.SIMPRINTS_REGISTRATION, registration)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
        subjectActions?.let {
            intent.putExtra(
                Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS,
                subjectActions
            )
        }
        sendOkResult(intent)
    }

    override fun returnIdentification(
        identifications: ArrayList<Identification>,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?
    ) = Intent().let { intent ->

        intent.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
        sendOkResult(intent)
    }

    override fun returnVerification(
        verification: Verification,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?
    ) = Intent().let { intent ->
        intent.putExtra(Constants.SIMPRINTS_VERIFICATION, verification)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
        sendOkResult(intent)
    }

    override fun returnRefusalForms(
        refusalForm: RefusalForm,
        sessionId: String,
        flowCompletedCheck: Boolean,
        eventsJson: String?
    ) = Intent().let { intent ->

        intent.putExtra(Constants.SIMPRINTS_REFUSAL_FORM, refusalForm)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
        sendOkResult(intent)
    }

    override fun returnConfirmation(
        identificationOutcome: Boolean,
        sessionId: String,
        eventsJson: String?
    ) = Intent().let { intent ->
        intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, identificationOutcome)
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
        sendOkResult(intent)
    }

    /**
     * Not being used because the project might use CoSync. Next method does the same but with a nullable eventsJson.
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
    ) {
        setResult(
            errorResponse.reason.libSimprintsResultCode(),
            Intent().let { intent ->
                intent.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
                intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
                eventsJson?.let { intent.putExtra(Constants.SIMPRINTS_COSYNC_EVENT, eventsJson) }
            }
        )
        finish()
    }

}
