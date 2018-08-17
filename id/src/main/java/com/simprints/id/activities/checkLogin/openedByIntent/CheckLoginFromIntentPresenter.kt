package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.events.SessionEventsManager
import com.simprints.id.data.analytics.events.models.*
import com.simprints.id.data.analytics.events.models.AuthorizationEvent.Info
import com.simprints.id.data.analytics.events.models.AuthorizationEvent.Result.AUTHORIZED
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.session.callout.Callout
import com.simprints.id.session.sessionParameters.SessionParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromIntentContract.Presenter {

    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var possibleLegacyApiKey: String = ""
    private var setupFailed: Boolean = false

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var dbManager: LocalDbManager

    init {
        component.inject(this)
    }

    override fun setup() {
        view.checkCallingAppIsFromKnownSource()

        try {
            val callout = view.parseCallout()
            extractSessionParametersOrThrow(callout)
            setLastUser()
        } catch (exception: InvalidCalloutError) {
            view.openAlertActivityForError(exception.alertType)
            setupFailed = true
        }
    }

    private fun setLastUser() {
        preferencesManager.lastUserUsed = preferencesManager.userId
    }

    override fun start() {
        if (!setupFailed) {
            checkSignedInStateAndMoveOn()
        }
    }

    private fun extractSessionParametersOrThrow(callout: Callout): SessionParameters {
        analyticsManager.logCallout(callout)
        val sessionParameters = sessionParametersExtractor.extractFrom(callout)
        possibleLegacyApiKey = sessionParameters.apiKey
        preferencesManager.sessionParameters = sessionParameters
        analyticsManager.logUserProperties()
        return sessionParameters
    }

    override fun handleNotSignedInUser() {
        sessionEventsManager.updateSessionInBackground({
            addAuthorizationEvent(it, AuthorizationEvent.Result.NOT_AUTHORIZED)
        })

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
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()
        initSessionEvents(loginInfoManager.getSignedInProjectIdOrEmpty())
        view.openLaunchActivity()
    }

    private fun initSessionEvents(projectId: String) {

        try {
            Singles.zip(
                sessionEventsManager.createSession(projectId),
                analyticsManager.analyticsId.onErrorReturn { "" },
                dbManager.getPeopleCountFromLocal().onErrorReturn { -1 }) { session: SessionEvents, gaId: String, count: Int ->
                    session.apply {
                        analyticsId = gaId
                        databaseInfo = DatabaseInfo(count)
                        events.apply {
                            add(CalloutEvent(session.nowRelativeToStartTime(timeHelper), view.parseCallout()))
                            add(view.buildConnectionEvent(session))
                            addAuthorizationEvent(session, AUTHORIZED)
                        }
                    }

                    return@zip session
            }.flatMapCompletable {
                sessionEventsManager.insertOrUpdateSession(it)
            }.subscribeBy(onComplete = { }, onError = { it.printStackTrace() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addAuthorizationEvent(session: SessionEvents, result: AuthorizationEvent.Result) {
        session.events.add(AuthorizationEvent(
            session.nowRelativeToStartTime(timeHelper),
            result,
            if (result == AUTHORIZED) {
                Info(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else { null }
        ))
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, returnCallout: Callout) {
        sessionEventsManager.updateSession({
            it.events.add(CallbackEvent(it.nowRelativeToStartTime(timeHelper), returnCallout))
            it.closeIfRequired(timeHelper)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
            }, onError = {
                it.printStackTrace()
            })
    }
}
