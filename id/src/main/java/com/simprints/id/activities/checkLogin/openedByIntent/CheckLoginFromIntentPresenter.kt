package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.callout.*
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse.fromDomainToModuleApiAppErrorResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppConfirmIdentityRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse.Reason
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload.Companion.buildEvent as buildConnectivitySnapshotEvent

class CheckLoginFromIntentPresenter(
    val view: CheckLoginFromIntentContract.View,
    val deviceId: String,
    component: AppComponent
) :
    CheckLoginPresenter(view, component),
    CheckLoginFromIntentContract.Presenter {

    @Inject
    lateinit var remoteConfigFetcher: RemoteConfigFetcher

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    @Inject
    lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository

    @Inject
    lateinit var subjectLocalDataSource: SubjectLocalDataSource

    @Inject
    lateinit var simNetworkUtils: SimNetworkUtils
    internal lateinit var appRequest: AppRequest

    init {
        component.inject(this)
    }

    override suspend fun setup() {
        try {
            parseAppRequest()
            addCalloutAndConnectivityEventsInSession(appRequest)

            extractSessionParametersForAnalyticsManager()
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

    private suspend fun addCalloutAndConnectivityEventsInSession(appRequest: AppRequest) {
        ignoreException {
            if (appRequest !is AppRequest.AppRequestFollowUp) {
                eventRepository.addOrUpdateEvent(
                    buildConnectivitySnapshotEvent(
                        simNetworkUtils,
                        timeHelper
                    )
                )
            }
            eventRepository.addOrUpdateEvent(buildRequestEvent(timeHelper.now(), appRequest))
        }
    }

    private fun buildRequestEvent(relativeStartTime: Long, request: AppRequest): Event =
        when (request) {
            is AppEnrolRequest -> buildEnrolmentCalloutEvent(request, relativeStartTime)
            is AppVerifyRequest -> buildVerificationCalloutEvent(request, relativeStartTime)
            is AppIdentifyRequest -> buildIdentificationCalloutEvent(request, relativeStartTime)
            is AppConfirmIdentityRequest -> addConfirmationCalloutEvent(request, relativeStartTime)
            is AppEnrolLastBiometricsRequest -> addEnrolLastBiometricsCalloutEvent(
                request,
                relativeStartTime
            )
        }

    private fun addEnrolLastBiometricsCalloutEvent(
        request: AppEnrolLastBiometricsRequest,
        relativeStarTime: Long
    ) =
        EnrolmentLastBiometricsCalloutEvent(
            relativeStarTime,
            request.projectId,
            request.userId,
            request.moduleId,
            request.metadata,
            request.identificationSessionId
        )

    private fun addConfirmationCalloutEvent(
        request: AppConfirmIdentityRequest,
        relativeStartTime: Long
    ) =
        ConfirmationCalloutEvent(
            relativeStartTime,
            request.projectId,
            request.selectedGuid,
            request.sessionId
        )

    private fun buildIdentificationCalloutEvent(
        request: AppIdentifyRequest,
        relativeStartTime: Long
    ) =
        with(request) {
            IdentificationCalloutEvent(
                relativeStartTime,
                projectId, userId, moduleId, metadata
            )
        }

    private fun buildVerificationCalloutEvent(request: AppVerifyRequest, relativeStartTime: Long) =
        with(request) {
            VerificationCalloutEvent(
                relativeStartTime,
                projectId, userId, moduleId, verifyGuid, metadata
            )
        }


    private fun buildEnrolmentCalloutEvent(request: AppEnrolRequest, relativeStartTime: Long) =
        with(request) {
            EnrolmentCalloutEvent(
                relativeStartTime,
                projectId, userId, moduleId, metadata
            )
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
        val domainErrorResponse =
            AppErrorResponse(Reason.fromDomainAlertTypeToAppErrorType(alertActResponse.alertType))
        view.setResultErrorAndFinish(fromDomainToModuleApiAppErrorResponse(domainErrorResponse))
    }

    override fun onLoginScreenErrorReturn(appErrorResponse: AppErrorResponse) {
        view.setResultErrorAndFinish(fromDomainToModuleApiAppErrorResponse(appErrorResponse))
    }

    private fun extractSessionParametersForAnalyticsManager() =
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
            inBackground {
                eventRepository.addOrUpdateEvent(
                    buildAuthorizationEvent(
                        AuthorizationResult.NOT_AUTHORIZED
                    )
                )
            }

            loginAlreadyTried.set(true)
            view.openLoginActivity(appRequest)
        } else {
            view.finishCheckLoginFromIntentActivity()
        }
    }

    /** @throws DifferentProjectIdSignedInException */
    override fun isProjectIdStoredAndMatches(): Boolean =
        loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty() &&
            matchProjectIdsOrThrow(
                loginInfoManager.getSignedInProjectIdOrEmpty(),
                appRequest.projectId
            )

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
        super.handleSignedInUser()
        Timber.d("[CHECK_LOGIN] User is signed in")

        /** Hack to support multiple users: If all login checks success, then we consider
         *  the userId in the Intent as new signed User
         *  Because we move ConfirmIdentity behind the login check, some integration
         *  doesn't have the userId in the intent. We don't want to switch the
         *  user otherwise will be set to "" and the following requests would fail.
         *  */
        if (appRequest.userId.isNotEmpty()) {
            loginInfoManager.signedInUserId = appRequest.userId
        }

        remoteConfigFetcher.doFetchInBackgroundAndActivateUsingDefaultCacheTime()

        updateProjectInCurrentSession()


        Timber.d("[CHECK_LOGIN] Updating events")

        CoroutineScope(Dispatchers.IO).launch {
            awaitAll(
                async { updateDatabaseCountsInCurrentSession() },
                async { addAuthorizedEventInCurrentSession() },
                async { initAnalyticsKeyInCrashManager() },
                async { updateAnalyticsIdInCurrentSession() }
            )

            withContext(Dispatchers.Main){
                Timber.d("[CHECK_LOGIN] Current session updated ${eventRepository.getCurrentCaptureSessionEvent()}")
                Timber.d("[CHECK_LOGIN] Moving to orchestrator")
                view.openOrchestratorActivity(appRequest)
            }
        }
    }

    private suspend fun addAuthorizedEventInCurrentSession() {
        eventRepository.addOrUpdateEvent(buildAuthorizationEvent(AuthorizationResult.AUTHORIZED))
        Timber.d("[CHECK_LOGIN] Added authorised event")
    }

    private suspend fun updateProjectInCurrentSession() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val signedProjectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (signedProjectId != currentSessionEvent.payload.projectId) {
            currentSessionEvent.updateProjectId(signedProjectId)
            currentSessionEvent.updateModalities(preferencesManager.modalities)
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }
        val associatedEvents = eventRepository.getEventsFromSession(currentSessionEvent.id)
        associatedEvents.collect {
            it.labels = it.labels.copy(projectId = signedProjectId)
            eventRepository.addOrUpdateEvent(it)
        }

        Timber.d("[CHECK_LOGIN] Updated projectId in current session")
    }

    private suspend fun updateDatabaseCountsInCurrentSession() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val payload = currentSessionEvent.payload
        payload.databaseInfo.recordCount = subjectLocalDataSource.count()

        eventRepository.addOrUpdateEvent(currentSessionEvent)
        Timber.d("[CHECK_LOGIN] Updated Database count in current session")
    }

    private fun initAnalyticsKeyInCrashManager() {
        crashReportManager.apply {
            setProjectIdCrashlyticsKey(loginInfoManager.getSignedInProjectIdOrEmpty())
            setUserIdCrashlyticsKey(loginInfoManager.getSignedInUserIdOrEmpty())
            setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
            setDownSyncTriggersCrashlyticsKey(preferencesManager.eventDownSyncSetting.toString())
            setFingersSelectedCrashlyticsKey(preferencesManager.fingerprintsToCollect.map { it.toString() })
        }
        Timber.d("[CHECK_LOGIN] Added keys in CrashManager")
    }

    private suspend fun updateAnalyticsIdInCurrentSession() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()
        currentSessionEvent.payload.analyticsId = analyticsManager.getAnalyticsId()
        eventRepository.addOrUpdateEvent(currentSessionEvent)
        Timber.d("[CHECK_LOGIN] Updated analytics id in current session")
    }

    private fun buildAuthorizationEvent(result: AuthorizationResult) =
        AuthorizationEvent(
            timeHelper.now(),
            result,
            if (result == AuthorizationResult.AUTHORIZED) {
                UserInfo(
                    loginInfoManager.getSignedInProjectIdOrEmpty(),
                    loginInfoManager.getSignedInUserIdOrEmpty()
                )
            } else {
                null
            }
        )


    @SuppressLint("CheckResult")
    private suspend fun setSessionIdCrashlyticsKey() {
        ignoreException {
            crashReportManager.setSessionIdCrashlyticsKey(eventRepository.getCurrentCaptureSessionEvent().id)
        }
    }
}
