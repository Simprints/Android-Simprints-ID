package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.AuthorizationEvent
import com.simprints.id.data.db.session.domain.models.events.ConnectivitySnapshotEvent
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.callout.*
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.tools.ignoreException
import com.simprints.id.tools.utils.SimNetworkUtils
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CheckLoginFromIntentPresenter(val view: CheckLoginFromIntentContract.View,
                                    val deviceId: String,
                                    component: AppComponent) :
    CheckLoginPresenter(view, component),
    CheckLoginFromIntentContract.Presenter {

    @Inject
    lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    @Inject
    lateinit var sessionRepository: SessionRepository
    @Inject
    lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject
    lateinit var simNetworkUtils: SimNetworkUtils
    internal lateinit var appRequest: AppRequest

    init {
        component.inject(this)
    }

    override suspend fun setup() {
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

    internal suspend fun addCalloutAndConnectivityEventsInSession(appRequest: AppRequest) {
        ignoreException {
            sessionRepository.updateCurrentSession { currentSession ->
                with(currentSession) {
                    addEvent(ConnectivitySnapshotEvent.buildEvent(simNetworkUtils, timeHelper))
                    addEvent(buildRequestEvent(timeHelper.now(), appRequest))
                }
            }
        }
    }

    internal fun buildRequestEvent(relativeStarTime: Long, request: AppRequest): Event =
        when (request) {
            is AppEnrolRequest -> buildEnrolmentCalloutEvent(request, relativeStarTime)
            is AppVerifyRequest -> buildVerificationCalloutEvent(request, relativeStarTime)
            is AppIdentifyRequest -> buildIdentificationCalloutEvent(request, relativeStarTime)
            is AppConfirmIdentityRequest -> addConfirmationCalloutEvent(request, relativeStarTime)
            is AppEnrolLastBiometricsRequest -> addEnrolLastBiometricsCalloutEvent(request, relativeStarTime)
        }

    internal fun addEnrolLastBiometricsCalloutEvent(request: AppEnrolLastBiometricsRequest, relativeStarTime: Long) =
        EnrolmentLastBiometricsCalloutEvent(
            relativeStarTime,
            request.projectId,
            request.userId,
            request.moduleId,
            request.metadata,
            request.identificationSessionId)

    internal fun addConfirmationCalloutEvent(request: AppConfirmIdentityRequest, relativeStarTime: Long) =
        ConfirmationCalloutEvent(
            relativeStarTime,
            request.projectId,
            request.selectedGuid,
            request.sessionId)

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

    override suspend fun start() {
        checkSignedInStateIfPossible()
    }

    override suspend fun checkSignedInStateIfPossible() {
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
            if (this is AppRequestFlow) {
                analyticsManager.logCallout(this)
                analyticsManager.logUserProperties(userId, projectId, moduleId, deviceId)
            }
        }

    override fun handleNotSignedInUser() {

        // The ConfirmIdentity should not be used to trigger the login, since if user is not signed in
        // there is not session open. (ClientApi doesn't create it for ConfirmIdentity)
        if (!loginAlreadyTried.get() && appRequest !is AppConfirmIdentityRequest && appRequest !is AppEnrolLastBiometricsRequest) {
            sessionRepository.addEventToCurrentSessionInBackground(buildAuthorizationEvent(AuthorizationEvent.Result.NOT_AUTHORIZED))

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
    override suspend fun handleSignedInUser() {
        /** Hack to support multiple users: If all login checks success, then we consider
         *  the userId in the Intent as new signed User */
        loginInfoManager.signedInUserId = appRequest.userId
        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        ignoreException {
            val peopleInDb = personLocalDataSource.count()
            sessionRepository.updateCurrentSession { currentSession ->
                val authorisationEvent = buildAuthorizationEvent(AuthorizationEvent.Result.AUTHORIZED)

                with(currentSession) {
                    addEvent(authorisationEvent)
                    projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
                    databaseInfo.recordCount = peopleInDb
                }
            }
        }

        view.openOrchestratorActivity(appRequest)
        initOrUpdateAnalyticsKeys()
    }

    private fun initOrUpdateAnalyticsKeys() {
        crashReportManager.apply {
            setProjectIdCrashlyticsKey(loginInfoManager.getSignedInProjectIdOrEmpty())
            setUserIdCrashlyticsKey(loginInfoManager.getSignedInUserIdOrEmpty())
            setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
            setDownSyncTriggersCrashlyticsKey(preferencesManager.peopleDownSyncSetting)
            setFingersSelectedCrashlyticsKey(preferencesManager.fingerStatus)
        }
    }

    internal suspend fun addAnalyticsInfoAndProjectId() {
        ignoreException {
            val analyticsId = analyticsManager.getAnalyticsId()
            sessionRepository.updateCurrentSession { currentSession ->
                val signedInProject = loginInfoManager.getSignedInProjectIdOrEmpty()
                if (signedInProject.isNotEmpty()) {
                    currentSession.projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
                }
                currentSession.analyticsId = analyticsId
            }
        }
    }

    private fun buildAuthorizationEvent(result: AuthorizationEvent.Result) =
        AuthorizationEvent(
            timeHelper.now(),
            result,
            if (result == AuthorizationEvent.Result.AUTHORIZED) {
                AuthorizationEvent.UserInfo(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
            } else {
                null
            }
        )


    @SuppressLint("CheckResult")
    private suspend fun setSessionIdCrashlyticsKey() {
        ignoreException {
            crashReportManager.setSessionIdCrashlyticsKey(sessionRepository.getCurrentSession().id)
        }
    }
}
