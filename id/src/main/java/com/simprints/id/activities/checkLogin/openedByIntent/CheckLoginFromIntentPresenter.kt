package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.domain.sessionParameters.extractors.Extractor
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
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

    override fun getUserId(): String = dataManager.userId

    override fun handleSignedInUser() {
        view.openLaunchActivity()
    }
}
