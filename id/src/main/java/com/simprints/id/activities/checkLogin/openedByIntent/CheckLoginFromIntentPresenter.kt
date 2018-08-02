package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.DatabaseInfo
import com.simprints.id.data.analytics.SessionEventsManager
import com.simprints.id.data.analytics.events.CalloutEvent
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.secure.cryptography.Hasher
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromIntentContract.Presenter {

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var possibleLegacyApiKey: String = ""
    private var setupFailed: Boolean = false

    @Inject lateinit var sessionEventManager: SessionEventsManager
    @Inject lateinit var dbManager: LocalDbManager

    init {
        component.inject(this)
    }

    override fun setup() {
        view.checkCallingAppIsFromKnownSource()

        try {
            extractSessionParameters()
            preferencesManager.lastUserUsed = preferencesManager.userId
            sessionEventManager.createSessionEvent()
            sessionEventManager.addEvent(CalloutEvent(view.parseCallout()))
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
        analyticsManager.logCallout(callout)
        val sessionParameters = sessionParametersExtractor.extractFrom(callout)
        possibleLegacyApiKey = sessionParameters.apiKey
        preferencesManager.sessionParameters = sessionParameters
        analyticsManager.logUserProperties()
    }

    override fun handleNotSignedInUser() {
        if (!loginAlreadyTried.get()) {
            loginAlreadyTried.set(true)
            view.openLoginActivity(possibleLegacyApiKey)
        } else {
            view.finishCheckLoginFromIntentActivity()
        }
    }

    /** @throws DifferentProjectIdSignedInException */
    override fun isProjectIdStoredAndMatches(): Boolean =
        loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty() &&
            matchIntentAndStoredProjectIdsOrThrow(loginInfoManager.getSignedInProjectIdOrEmpty())

    private fun matchIntentAndStoredProjectIdsOrThrow(storedProjectId: String): Boolean =
        if (possibleLegacyApiKey.isEmpty()) {
            matchProjectIdsOrThrow(storedProjectId, preferencesManager.projectId)
        } else {
            val hashedLegacyApiKey = Hasher().hash(possibleLegacyApiKey)
            val storedProjectIdFromLegacyOrEmpty = loginInfoManager.getProjectIdForHashedLegacyProjectIdOrEmpty(hashedLegacyApiKey)
            matchProjectIdsOrThrow(storedProjectId, storedProjectIdFromLegacyOrEmpty)
        }

    private fun matchProjectIdsOrThrow(storedProjectId: String, intentProjectId: String): Boolean =
        storedProjectId == intentProjectId ||
            throw DifferentProjectIdSignedInException()

    /** @throws DifferentUserIdSignedInException */
    override fun isUserIdStoredAndMatches() =
        if (preferencesManager.userId != loginInfoManager.getSignedInUserIdOrEmpty())
            throw DifferentUserIdSignedInException()
        else loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()

    override fun handleSignedInUser() {
        updateEventSessionWithDbInfo()
        view.openLaunchActivity()
    }

    private fun updateEventSessionWithDbInfo() {
        dbManager.getPeopleCountFromLocal().subscribeBy(onError = {
            it.printStackTrace()
        }, onSuccess = {
            sessionEventManager.updateDatabaseInfo(DatabaseInfo(it))
        })
    }
}
