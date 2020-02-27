package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.models.events.AuthorizationEvent
import com.simprints.id.data.db.session.domain.models.events.ConnectivitySnapshotEvent
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.VerificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    val deviceId: String,
                                    component: AppComponent) :
    CheckLoginPresenter(view, component),
    CheckLoginFromIntentContract.Presenter {

    @Inject lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject lateinit var simNetworkUtils: SimNetworkUtils
    internal lateinit var appRequest: AppRequest

    init {
        component.inject(this)
    }

    override fun setup() {
        try {
            addAnalyticsInfoAndProjectId()
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
        view.setResultErrorAndFinish(fromDomainToModuleApiAppErrorResponse(domainErrorResponse))
    }

    override fun onLoginScreenErrorReturn(appErrorResponse: AppErrorResponse) {
        view.setResultErrorAndFinish(fromDomainToModuleApiAppErrorResponse(appErrorResponse))
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

    @SuppressLint("CheckResult")
    override fun handleSignedInUser() {
        /** Hack to support multiple users: If all login checks success, then we consider
         *  the userId in the Intent as new signed User */
        loginInfoManager.signedInUserId = appRequest.userId
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        fetchPeopleCountInLocalDatabase().flatMapCompletable { recordCount ->
            sessionEventsManager.updateSession {
                it.events.apply {
                    addAuthorizationEvent(it, AuthorizationEvent.Result.AUTHORIZED)
                }
                it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
                it.databaseInfo.recordCount = recordCount
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                Timber.d("Session updated")
            }, onError = {
                it.printStackTrace()
            })

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

    internal fun addAnalyticsInfoAndProjectId() =
        fetchAnalyticsId()
            .flatMapCompletable { gaId ->
                sessionEventsManager.updateSession {
                    val signedInProject = loginInfoManager.getSignedInProjectIdOrEmpty()
                    if (signedInProject.isNotEmpty()) {
                        it.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
                    }
                    it.analyticsId = gaId
                }
            }.subscribeBy(onError = { it.printStackTrace() })

    private fun fetchAnalyticsId() =
        analyticsManager.analyticsId.onErrorReturn { "" }

    private fun fetchPeopleCountInLocalDatabase(): Single<Int> = singleWithSuspend { personLocalDataSource.count() }

    private fun addAuthorizationEvent(session: SessionEvents, result: AuthorizationEvent.Result) {
        session.addEvent(AuthorizationEvent(
            timeHelper.now(),
            result,
            if (result == AuthorizationEvent.Result.AUTHORIZED) {
                AuthorizationEvent.UserInfo(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else {
                null
            }
        ))
    }

    @SuppressLint("CheckResult")
    private fun setSessionIdCrashlyticsKey() {
        sessionEventsManager.getCurrentSession()
            .subscribeBy(onSuccess = {
                crashReportManager.setSessionIdCrashlyticsKey(it.id)
            }, onError = {
                it.printStackTrace()
            })
    }
}
