package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.AuthorizationEvent
import com.simprints.id.data.analytics.eventData.models.events.AuthorizationEvent.Info
import com.simprints.id.data.analytics.eventData.models.events.AuthorizationEvent.Result.AUTHORIZED
import com.simprints.id.data.analytics.eventData.models.events.CallbackEvent
import com.simprints.id.data.analytics.eventData.models.events.CalloutEvent
import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.session.callout.Callout
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromIntentContract.Presenter {

    @Inject
    lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var possibleLegacyApiKey: String = ""
    private var setupFailed: Boolean = false

    @Inject
    lateinit var sessionEventsManager: SessionEventsManager
    @Inject
    lateinit var dbManager: LocalDbManager

    init {
        component.inject(this)
    }

    override fun setup() {
        view.checkCallingAppIsFromKnownSource()

        try {
            extractSessionParametersOrThrow()
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

    private fun extractSessionParametersOrThrow() {
        val callout = view.parseCallout()
        analyticsManager.logCallout(callout)
        val sessionParameters = sessionParametersExtractor.extractFrom(callout)
        possibleLegacyApiKey = sessionParameters.apiKey
        preferencesManager.sessionParameters = sessionParameters
        analyticsManager.logUserProperties()
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
                createSessionEvents(projectId),
                fetchAnalyticsId(),
                fetchPeopleCountInLocalDatabase()) { _: SessionEvents, gaId: String, dbCount: Int ->
                return@zip Pair(gaId, dbCount)

            }.flatMapCompletable { gaIdAndDbCount ->
                populateSessionWithAnalyticsIdAndDbInfo(gaIdAndDbCount.first, gaIdAndDbCount.second)
            }.subscribeBy(onError = { it.printStackTrace() })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createSessionEvents(projectId: String): Single<SessionEvents> = sessionEventsManager.createSession(projectId)
    private fun fetchAnalyticsId(): Single<String> = analyticsManager.analyticsId.onErrorReturn { "" }
    private fun fetchPeopleCountInLocalDatabase(): Single<Int> = dbManager.getPeopleCountFromLocal().onErrorReturn { -1 }
    private fun populateSessionWithAnalyticsIdAndDbInfo(gaId: String, dbCount: Int): Completable =
        sessionEventsManager.updateSession({
            it.analyticsId = gaId
            it.databaseInfo = DatabaseInfo(dbCount)
            it.events.apply {
                add(CalloutEvent(it.nowRelativeToStartTime(timeHelper), view.parseCallout()))
                add(view.buildConnectionEvent(it))
                addAuthorizationEvent(it, AUTHORIZED)
            }
        })

    private fun addAuthorizationEvent(session: SessionEvents, result: AuthorizationEvent.Result) {
        session.events.add(AuthorizationEvent(
            session.nowRelativeToStartTime(timeHelper),
            result,
            if (result == AUTHORIZED) {
                Info(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else {
                null
            }
        ))
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, returnCallout: Callout) {
        sessionEventsManager.updateSession({
            it.events.add(CallbackEvent(it.nowRelativeToStartTime(timeHelper), returnCallout))
            it.closeIfRequired(timeHelper)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {}, onError = {
                it.printStackTrace()
            })
    }
}
