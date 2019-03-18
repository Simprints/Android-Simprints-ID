package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent.Result.AUTHORIZED
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent.UserInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.requests.EnrolRequest
import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest
import com.simprints.id.domain.responses.*
import com.simprints.id.exceptions.safe.callout.InvalidCalloutError
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.tools.utils.SimNetworkUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromIntentContract.Presenter {

    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var dbManager: LocalDbManager
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    private val appRequest = view.parseRequest()

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun setup() {
        view.checkCallingAppIsFromKnownSource()
        sessionEventsManager.createSession(view.getAppVersionNameFromPackageManager()).doFinally {
            try {
                extractSessionParametersOrThrow()
                addCalloutAndConnectivityEventsInSession(appRequest)
                setLastUser()
                setSessionIdCrashlyticsKey()
            } catch (exception: InvalidCalloutError) {
                view.openAlertActivityForError(exception.alert)
                setupFailed = true
            }
        }.subscribeBy(onError = { it.printStackTrace() })
    }

    private fun addCalloutAndConnectivityEventsInSession(appRequest: Request) {
        sessionEventsManager.updateSessionInBackground {
            it.events.apply {
                add(ConnectivitySnapshotEvent.buildEvent(simNetworkUtils, it, timeHelper))
                add(buildRequestEvent(it.timeRelativeToStartTime(timeHelper.now()), appRequest))
            }
        }
    }

    private fun buildRequestEvent(relativeStarTime: Long, request: Request): Event =
        when (request) {
            is EnrolRequest -> EnrolRequestEvent(relativeStarTime, request)
            is VerifyRequest -> VerifyRequestEvent(relativeStarTime, request)
            is IdentifyRequest -> IdentifyRequestEvent(relativeStarTime, request)
            else -> throw Throwable("unrecognised request") //StopShip
        }


    private fun buildResponseEvent(relativeStarTime: Long, response: Response?): Event =
        if (response == null) {
            NoResponseEvent(relativeStarTime)
        } else {
            when (response) {
                is EnrolResponse -> EnrolResponseEvent(relativeStarTime, response)
                is VerifyResponse -> VerifyResponseEvent(relativeStarTime, response)
                is IdentifyResponse -> IdentifyResponseEvent(relativeStarTime, response)
                is RefusalFormResponse -> RefusalFormResponseEvent(relativeStarTime, response)
                else -> throw Throwable("unrecognised request") //StopShip
            }
        }

    private fun setLastUser() {
        preferencesManager.lastUserUsed = appRequest.userId
    }

    override fun start() {
        if (!setupFailed) {
            checkSignedInStateAndMoveOn()
        }
    }

    private fun extractSessionParametersOrThrow() {
        analyticsManager.logCallout(appRequest)
        analyticsManager.logUserProperties(appRequest.userId, appRequest.projectId, appRequest.moduleId, view.getDeviceUniqueId())
    }

    override fun handleNotSignedInUser() {
        sessionEventsManager.updateSessionInBackground {
            addAuthorizationEvent(it, AuthorizationEvent.Result.NOT_AUTHORIZED)
        }

        if (!loginAlreadyTried.get()) {
            loginAlreadyTried.set(true)
            view.openLoginActivity(appRequest)
        } else {
            view.finishCheckLoginFromIntentActivity()
        }
    }

    /** @throws DifferentProjectIdSignedInException */
    override fun isProjectIdStoredAndMatches(): Boolean =
        loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty() &&
            matchProjectIdsOrThrow(loginInfoManager.getSignedInProjectIdOrEmpty(), appRequest.projectId)

    private fun matchProjectIdsOrThrow(storedProjectId: String, intentProjectId: String): Boolean =
        storedProjectId == intentProjectId ||
            throw DifferentProjectIdSignedInException()

    /** @throws DifferentUserIdSignedInException */
    override fun isUserIdStoredAndMatches() =
    //if (dataManager.userId != dataManager.getSignedInUserIdOrEmpty())
    //    throw DifferentUserIdSignedInException()
    //else
        /** Hack to support multiple users: we do not check if the signed UserId
        matches the userId from the Intent */
        loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()

    override fun handleSignedInUser() {
        /** Hack to support multiple users: If all login checks success, then we consider
         *  the userId in the Intent as new signed User */
        loginInfoManager.signedInUserId = appRequest.userId
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()
        addInfoIntoSessionEventsAfterUserSignIn()
        view.openLaunchActivity(appRequest)
        initOrUpdateAnalyticsKeys()
    }

    private fun initOrUpdateAnalyticsKeys() {
        crashReportManager.apply {
            setProjectIdCrashlyticsKey(loginInfoManager.getSignedInProjectIdOrEmpty())
            setUserIdCrashlyticsKey(loginInfoManager.getSignedInUserIdOrEmpty())
            setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
            setDownSyncTriggersCrashlyticsKey(preferencesManager.peopleDownSyncTriggers)
            setFingersSelectedCrashlyticsKey(preferencesManager.fingerStatus)
        }
    }

    @SuppressLint("CheckResult")
    private fun addInfoIntoSessionEventsAfterUserSignIn() {
        try {
            Singles.zip(
                fetchAnalyticsId(),
                fetchPeopleCountInLocalDatabase(),
                fetchSessionCountInLocalDatabase()) { gaId: String, peopleDbCount: Int, sessionDbCount: Int ->
                return@zip Triple(gaId, peopleDbCount, sessionDbCount)
            }.flatMapCompletable { (gaId, peopleDbCount, sessionDbCount) ->
                populateSessionWithAnalyticsIdAndDbInfo(gaId, peopleDbCount, sessionDbCount)
            }.subscribeBy(onError = { it.printStackTrace() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchAnalyticsId(): Single<String> = analyticsManager.analyticsId.onErrorReturn { "" }
    private fun fetchPeopleCountInLocalDatabase(): Single<Int> = dbManager.getPeopleCountFromLocal().onErrorReturn { -1 }
    private fun fetchSessionCountInLocalDatabase(): Single<Int> = sessionEventsManager.getSessionCount().onErrorReturn { -1 }
    private fun populateSessionWithAnalyticsIdAndDbInfo(gaId: String, peopleDbCount: Int, sessionDbCount: Int): Completable =
        sessionEventsManager.updateSession {
            it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            it.analyticsId = gaId
            it.databaseInfo = DatabaseInfo(peopleDbCount, sessionDbCount)
            it.events.apply {
                addAuthorizationEvent(it, AUTHORIZED)
            }
        }

    private fun addAuthorizationEvent(session: SessionEvents, result: AuthorizationEvent.Result) {
        session.addEvent(AuthorizationEvent(
            session.timeRelativeToStartTime(timeHelper.now()),
            result,
            if (result == AUTHORIZED) {
                UserInfo(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else {
                null
            }
        ))
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, response: Response?) {
        sessionEventsManager.updateSessionInBackground {
            it.addEvent(buildResponseEvent(it.timeRelativeToStartTime(timeHelper.now()), response)) //STOPSHIP: Fix me
            it.closeIfRequired(timeHelper)
        }
    }

    @SuppressLint("CheckResult")
    private fun setSessionIdCrashlyticsKey() {
        sessionEventsManager.getCurrentSession().subscribeBy {
            crashReportManager.setSessionIdCrashlyticsKey(it.id)
        }
    }
}
