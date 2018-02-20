package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.domain.sessionParameters.extractors.Extractor
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.TimeHelper

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    val dataManager: DataManager,
                                    private val sessionParametersExtractor: Extractor<SessionParameters>,
                                    timeHelper: TimeHelper) :
    CheckLoginPresenter(dataManager, timeHelper), CheckLoginFromIntentContract.Presenter {

    private var started: Boolean = false

    init {
        view.setPresenter(this)
    }

    override fun start() {
        if (!started) {
            started = true
            initSession()
            view.checkCallingApp()

            // extracts the sessions Params (if not done before)
            try {
                extractSessionParameters()
            } catch (exception: InvalidCalloutError) {
                view.launchAlertForError(exception.alertType)
                return
            }
            openNextActivity()
        }
    }

    private fun extractSessionParameters() {
        val callout = view.parseCallout()
        callout.apply {
            dataManager.logCallout(this)
        }
        val sessionParameters = sessionParametersExtractor.extractFrom(callout)
        dataManager.sessionParameters = sessionParameters
        dataManager.calloutAction = sessionParameters.calloutAction
        dataManager.logUserProperties()
    }

    override fun openActivityForUserNotSignedIn() {
        view.openLoginActivity()
    }

    override fun openActivityForUserSignedIn() {
        view.openLaunchActivity()
    }

    override fun dbInitFailed(){
        view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    override fun isUserSignedInForStoredProjectId(): Boolean {
        val storedProjectId = dataManager.getSignedInProjectIdOrEmpty()
        return dataManager.projectId.let {
            if (it.isNotEmpty()) {
                it == storedProjectId
            } else {
                findProjectIdForApiKey(dataManager.apiKey) == storedProjectId
            }
        }
    }

    private fun findProjectIdForApiKey(legacyApiKey: String): String {
        return dataManager.projectIdForLegacyApiKeyOrEmpty(legacyApiKey)
    }
}
