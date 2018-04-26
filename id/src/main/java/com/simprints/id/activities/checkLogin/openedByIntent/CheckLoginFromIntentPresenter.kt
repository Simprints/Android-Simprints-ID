package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.session.sessionParameters.SessionParameters
import com.simprints.id.session.sessionParameters.extractors.Extractor
import com.simprints.id.tools.TimeHelper

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    val dataManager: DataManager,
                                    private val sessionParametersExtractor: Extractor<SessionParameters>,
                                    timeHelper: TimeHelper) :
    CheckLoginPresenter(view, dataManager, timeHelper), CheckLoginFromIntentContract.Presenter {

    private var loginAlreadyTried: Boolean = false
    private var possibleLegacyApiKey: String = ""
    private var setupFailed: Boolean = false

    override fun setup() {
        view.checkCallingAppIsFromKnownSource()

        try {
            extractSessionParameters()
            dataManager.lastUserUsed = dataManager.userId
        } catch (exception: InvalidCalloutError) {
            view.openAlertActivityForError(exception.alertType)
            setupFailed = true
        }
    }

    override fun start() {
        if (!setupFailed) {
            checkSignedInStateAndMoveOn()
        }
    }

    private fun extractSessionParameters() {
        val callout = view.parseCallout()
        dataManager.logCallout(callout)
        val sessionParameters = sessionParametersExtractor.extractFrom(callout)
        possibleLegacyApiKey = sessionParameters.apiKey
        dataManager.sessionParameters = sessionParameters
        dataManager.logUserProperties()
    }

    override fun handleNotSignedInUser() {
        if (!loginAlreadyTried) {
            loginAlreadyTried = true
            view.openLoginActivity(possibleLegacyApiKey)
        } else {
            view.finishCheckLoginFromIntentActivity()
        }
    }

    /** @throws DifferentProjectIdSignedInException */
    override fun isProjectIdStoredAndMatches(): Boolean =
        dataManager.getSignedInProjectIdOrEmpty().isNotEmpty() &&
            matchIntentAndStoredProjectIdsOrThrow(dataManager.getSignedInProjectIdOrEmpty())

    private fun matchIntentAndStoredProjectIdsOrThrow(storedProjectId: String): Boolean =
        if (possibleLegacyApiKey.isEmpty()) {
            matchProjectIdsOrThrow(storedProjectId, dataManager.projectId)
        } else {
            val hashedLegacyApiKey = Hasher().hash(possibleLegacyApiKey)
            val storedProjectIdFromLegacyOrEmpty = dataManager.getProjectIdForHashedLegacyProjectIdOrEmpty(hashedLegacyApiKey)
            matchProjectIdsOrThrow(storedProjectId, storedProjectIdFromLegacyOrEmpty)
        }

    private fun matchProjectIdsOrThrow(storedProjectId: String, intentProjectId: String): Boolean =
        storedProjectId == intentProjectId ||
            throw DifferentProjectIdSignedInException()

    /** @throws DifferentUserIdSignedInException */
    override fun isUserIdStoredAndMatches() =
        if (dataManager.userId != dataManager.getSignedInUserIdOrEmpty())
            throw DifferentUserIdSignedInException()
        else
            dataManager.getSignedInUserIdOrEmpty().isNotEmpty()

    override fun handleSignedInUser() {
        view.openLaunchActivity()
    }
}
