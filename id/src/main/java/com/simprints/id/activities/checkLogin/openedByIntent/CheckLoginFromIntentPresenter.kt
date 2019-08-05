package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent.Result.AUTHORIZED
import com.simprints.id.data.analytics.eventdata.models.domain.events.AuthorizationEvent.UserInfo
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConnectivitySnapshotEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse.fromDomainToAppErrorResponse
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.tools.utils.SimNetworkUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    val deviceId: String,
                                    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromIntentContract.Presenter {

    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var dbManager: LocalDbManager
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    internal lateinit var appRequest: AppRequest

    init {
        component.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun setup() {
        try {
            parseAppRequest()
            extractSessionParametersOrThrow()
            addCalloutAndConnectivityEventsInSession(appRequest)
            setLastUser()
            setSessionIdCrashlyticsKey()
        } catch (t: Throwable) {
            Timber.d(t)
            crashReportManager.logExceptionOrSafeException(t)
            view.openAlertActivityForError(AlertType.UNEXPECTED_ERROR)
            setupFailed = true
        }
    }

    private fun parseAppRequest() {
        appRequest = view.parseRequest()
    }

    internal fun addCalloutAndConnectivityEventsInSession(appRequest: AppRequest) {
        sessionEventsManager.updateSessionInBackground {
            it.events.apply {
                add(ConnectivitySnapshotEvent.buildEvent(simNetworkUtils, timeHelper))
                add(buildRequestEvent(timeHelper.now(), appRequest))
            }
        }
    }

    internal fun buildRequestEvent(relativeStarTime: Long, request: AppRequest): Event =
        when (request) {
            is AppEnrolRequest -> buildEnrolmentCalloutEvent(request, relativeStarTime)
            is AppVerifyRequest -> buildVerificationCalloutEvent(request, relativeStarTime)
            is AppIdentifyRequest -> buildIdentificationCalloutEvent(request, relativeStarTime)
            else -> throw InvalidAppRequest()
        }

    internal fun buildIdentificationCalloutEvent(request: AppIdentifyRequest, relativeStarTime: Long) =
        with(request) {
            IdentificationCalloutEvent(
                relativeStarTime,
                projectId, userId, moduleId, metadata)
        }

    internal fun buildVerificationCalloutEvent(request: AppVerifyRequest, relativeStarTime: Long) =
        with(request) {
            VerificationCalloutEvent(
                relativeStarTime,
                projectId, userId, moduleId, verifyGuid, metadata)
        }


    internal fun buildEnrolmentCalloutEvent(request: AppEnrolRequest, relativeStarTime: Long) =
        with(request) {
            EnrolmentCalloutEvent(
                relativeStarTime,
                projectId, userId, moduleId, metadata)
        }

    private fun setLastUser() {
        preferencesManager.lastUserUsed = appRequest.userId
    }

    override fun start() {
        checkSignedInStateIfPossible()
    }

    override fun checkSignedInStateIfPossible() {
        if (!setupFailed) {
            checkSignedInStateAndMoveOn()
        }
    }

    override fun onAlertScreenReturn(alertActResponse: AlertActResponse) {
        val domainErrorResponse = AppErrorResponse(Reason.fromDomainAlertTypeToAppErrorType(alertActResponse.alertType))
        view.setResultErrorAndFinish(fromDomainToAppErrorResponse(domainErrorResponse))
    }

    override fun onLoginScreenErrorReturn(appErrorResponse: AppErrorResponse) {
        view.setResultErrorAndFinish(fromDomainToAppErrorResponse(appErrorResponse))
    }

    private fun extractSessionParametersOrThrow() =
        with(appRequest) {
            analyticsManager.logCallout(this)
            analyticsManager.logUserProperties(userId, projectId, moduleId, deviceId)
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

        view.openOrchestratorActivity(appRequest)

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
                populateSessionWithAnalyticsIdAndDbInfo(gaId, peopleDbCount)
            }.subscribeBy(onError = { it.printStackTrace() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchAnalyticsId(): Single<String> = analyticsManager.analyticsId.onErrorReturn { "" }
    private fun fetchPeopleCountInLocalDatabase(): Single<Int> = dbManager.getPeopleCountFromLocal().onErrorReturn { -1 }
    private fun fetchSessionCountInLocalDatabase(): Single<Int> = sessionEventsManager.getSessionCount().onErrorReturn { -1 }
    private fun populateSessionWithAnalyticsIdAndDbInfo(gaId: String, peopleDbCount: Int): Completable =
        sessionEventsManager.updateSession {
            it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
            it.analyticsId = gaId
            it.databaseInfo.recordCount = peopleDbCount
            it.events.apply {
                addAuthorizationEvent(it, AUTHORIZED)
            }
        }

    private fun addAuthorizationEvent(session: SessionEvents, result: AuthorizationEvent.Result) {
        session.addEvent(AuthorizationEvent(
            timeHelper.now(),
            result,
            if (result == AUTHORIZED) {
                UserInfo(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else {
                null
            }
        ))
    }

    @SuppressLint("CheckResult")
    private fun setSessionIdCrashlyticsKey() {
        sessionEventsManager.getCurrentSession().subscribeBy {
            crashReportManager.setSessionIdCrashlyticsKey(it.id)
        }
    }
}
