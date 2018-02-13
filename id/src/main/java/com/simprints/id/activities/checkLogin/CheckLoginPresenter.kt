package com.simprints.id.activities.checkLogin

import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.DataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.domain.sessionParameters.extractors.Extractor
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.TimeHelper
import java.util.*

class CheckLoginPresenter(val view: CheckLoginContract.View,
                          val dataManager: DataManager,
                          private val sessionParametersExtractor: Extractor<SessionParameters>,
                          override var wasAppOpenedByIntent: Boolean,
                          private val timeHelper: TimeHelper) : CheckLoginContract.Presenter {

    private var started: Boolean = false

    init {
        view.setPresenter(this)
    }

    private val nextActivityClassAfterLogin by lazy {
        if (wasAppOpenedByIntent /* FUTURE: && calloutAction != LOGIN */) {
            LaunchActivity::class.java
        } else {
            DashboardActivity::class.java
        }
    }

    override fun start() {
        if (!started) {
            started = true
            initSession()

            // If app was launched by intent, we extract the sessions Params (if not done before)
            if (wasAppOpenedByIntent) {
                try {
                    extractSessionParameters()
                } catch (exception: InvalidCalloutError) {
                    view.launchAlertForError(exception.alertType)
                    return
                }
            }
            view.checkCallingApp()
            checkIfUserIsLoggedIn()
        }
    }

    override fun checkIfUserIsLoggedIn() {
        if (isUserSignedIn()) {
            initDbContext(dataManager.signedInProjectId)
            startNormalFlow()
        } else {
            redirectUserForLogin()
        }
    }

    private fun initDbContext(projectId: String) {
        if (!dataManager.isDbInitialised(projectId)) {
            try {
                dataManager.initialiseDb(projectId)
            } catch (error: UninitializedDataManagerError) {
                dataManager.logError(error)
                view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
            }
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

    private fun isUserSignedIn(): Boolean {
        val encProjectSecret = dataManager.getEncryptedProjectSecretOrEmpty()
        var storedProjectId = dataManager.getSignedInProjectIdOrEmpty()
        //val isFirebaseTokenValid = dataManager.isSignedIn(storedProjectId)
        val isFirebaseTokenValid = true

        if (encProjectSecret.isEmpty() || storedProjectId.isEmpty() || !isFirebaseTokenValid) {
            return false
        }

        return if (wasAppOpenedByIntent) {
            var projectIdFromIntent = dataManager.projectId
            if (projectIdFromIntent.isEmpty()) { //Legacy App with ApiKey
                projectIdFromIntent = findProjectIdForApiKey(dataManager.apiKey)
            }
            projectIdFromIntent == storedProjectId
        } else {
            true
        }
    }

    private fun findProjectIdForApiKey(legacyApiKey: String): String {
        return dataManager.projectIdForLegacyApiKeyOrEmpty(legacyApiKey)
    }

    private fun startNormalFlow() {
        view.startActivity(nextActivityClassAfterLogin)
    }

    private fun redirectUserForLogin() {
        if (wasAppOpenedByIntent) {
            view.openLoginActivity()
        } else {
            view.openRequestLoginActivity()
        }
    }

    private fun initSession() {
        dataManager.initializeSessionState(newSessionId(), timeHelper.msSinceBoot())
    }

    private fun newSessionId(): String {
        return UUID.randomUUID().toString()
    }
}
