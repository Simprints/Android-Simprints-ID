package com.simprints.id.activities.checkLogin.openedByIntent

import android.annotation.SuppressLint
import com.simprints.core.DeviceID
import com.simprints.core.DispatcherBG
import com.simprints.core.ExternalScope
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginPresenter
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
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AuthorizationEvent
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.callout.*
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.MODULE_IDS
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.PROJECT_ID
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.SESSION_ID
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.SUBJECTS_DOWN_SYNC_TRIGGERS
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.USER_ID
import com.simprints.infra.logging.Simber
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload.Companion.buildEvent as buildConnectivitySnapshotEvent

class CheckLoginFromIntentPresenter @AssistedInject constructor(
    @Assisted private val view: CheckLoginFromIntentContract.View,
    @DeviceID private val deviceId: String,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val eventRepository: EventRepository,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val simNetworkUtils: SimNetworkUtils,
    @ExternalScope private val externalScope: CoroutineScope,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) :
    CheckLoginPresenter(view),
    CheckLoginFromIntentContract.Presenter {

    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)
    private var setupFailed: Boolean = false

    internal lateinit var appRequest: AppRequest

    override suspend fun onViewCreated(isRestored: Boolean) {
        setup()
        // Prevent starting OrchestratorActivity twice when restoring the activity
        if (!isRestored) {
            start()
        }
    }

    private suspend fun setup() {
        try {
            parseAppRequest()
            showConfirmationTextIfPossible()
            addCalloutAndConnectivityEventsInSession(appRequest)

            extractSessionParametersForAnalyticsManager()
            setLastUser()
            setSessionIdCrashlyticsKey()
        } catch (t: Throwable) {
            Simber.e(t)
            view.openAlertActivityForError(AlertType.UNEXPECTED_ERROR)
            setupFailed = true
        }
    }

    private fun showConfirmationTextIfPossible() {
        if (appRequest is AppConfirmIdentityRequest) {
            view.showConfirmationText()
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

    private suspend fun setLastUser() {
        recentUserActivityManager.updateRecentUserActivity {
            it.apply {
                it.lastUserUsed = appRequest.userId
            }
        }
    }

    private suspend fun start() {
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
                Simber.tag(AnalyticsUserProperties.USER_ID, true).i(userId)
                Simber.tag(AnalyticsUserProperties.PROJECT_ID).i(projectId)
                Simber.tag(AnalyticsUserProperties.MODULE_ID).i(moduleId)
                Simber.tag(AnalyticsUserProperties.DEVICE_ID).i(deviceId)
            }
        }

    override fun handleNotSignedInUser() {
        // The ConfirmIdentity should not be used to trigger the login, since if user is not signed in
        // there is not session open. (ClientApi doesn't create it for ConfirmIdentity)
        if (!loginAlreadyTried.get() && appRequest !is AppConfirmIdentityRequest && appRequest !is AppEnrolLastBiometricsRequest) {
            externalScope.launch {
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
        loginManager.getSignedInProjectIdOrEmpty().isNotEmpty() &&
            matchProjectIdsOrThrow(
                loginManager.getSignedInProjectIdOrEmpty(),
                appRequest.projectId
            )

    private fun matchProjectIdsOrThrow(storedProjectId: String, intentProjectId: String): Boolean =
        if (storedProjectId == intentProjectId)
            true
        else
            throw DifferentProjectIdSignedInException()

    /** @throws DifferentUserIdSignedInException */
    override fun isUserIdStoredAndMatches() =
    //if (dataManager.userId != dataManager.getSignedInUserIdOrEmpty())
    //    throw DifferentUserIdSignedInException()
    //else
        /** Hack to support multiple users: we do not check if the signed UserId
        matches the userId from the Intent */
        loginManager.getSignedInUserIdOrEmpty().isNotEmpty()

    @SuppressLint("CheckResult")
    override suspend fun handleSignedInUser() {
        super.handleSignedInUser()
        Simber.d("[CHECK_LOGIN] User is signed in")

        /** Hack to support multiple users: If all login checks success, then we consider
         *  the userId in the Intent as new signed User
         *  Because we move ConfirmIdentity behind the login check, some integration
         *  doesn't have the userId in the intent. We don't want to switch the
         *  user otherwise will be set to "" and the following requests would fail.
         *  */
        if (appRequest.userId.isNotEmpty()) {
            loginManager.signedInUserId = appRequest.userId
        }

        updateProjectInCurrentSession()

        Simber.d("[CHECK_LOGIN] Updating events")
        CoroutineScope(dispatcher).launch {
            awaitAll(
                async { updateDatabaseCountsInCurrentSession() },
                async { addAuthorizedEventInCurrentSession() },
                async { initAnalyticsKeyInCrashManager() }
            )
        }.join()

        Simber.d("[CHECK_LOGIN] Current session updated ${eventRepository.getCurrentCaptureSessionEvent()}")
        Simber.d("[CHECK_LOGIN] Moving to orchestrator")
        view.openOrchestratorActivity(appRequest)
    }

    private suspend fun addAuthorizedEventInCurrentSession() {
        eventRepository.addOrUpdateEvent(buildAuthorizationEvent(AuthorizationResult.AUTHORIZED))
        Simber.d("[CHECK_LOGIN] Added authorised event")
    }

    private suspend fun updateProjectInCurrentSession() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val signedProjectId = loginManager.getSignedInProjectIdOrEmpty()
        if (signedProjectId != currentSessionEvent.payload.projectId) {
            val projectConfiguration = configManager.getProjectConfiguration()
            currentSessionEvent.updateProjectId(signedProjectId)
            currentSessionEvent.updateModalities(projectConfiguration.general.modalities)
            eventRepository.addOrUpdateEvent(currentSessionEvent)
        }
        val associatedEvents = eventRepository.observeEventsFromSession(currentSessionEvent.id)
        associatedEvents.collect {
            it.labels = it.labels.copy(projectId = signedProjectId)
            eventRepository.addOrUpdateEvent(it)
        }

        Simber.d("[CHECK_LOGIN] Updated projectId in current session")
    }

    private suspend fun updateDatabaseCountsInCurrentSession() {
        val currentSessionEvent = eventRepository.getCurrentCaptureSessionEvent()

        val payload = currentSessionEvent.payload
        payload.databaseInfo.recordCount = enrolmentRecordManager.count()

        eventRepository.addOrUpdateEvent(currentSessionEvent)
        Simber.d("[CHECK_LOGIN] Updated Database count in current session")
    }

    private suspend fun initAnalyticsKeyInCrashManager() {
        val projectConfiguration = configManager.getProjectConfiguration()
        val deviceConfiguration = configManager.getDeviceConfiguration()
        Simber.tag(PROJECT_ID, true).i(loginManager.getSignedInProjectIdOrEmpty())
        Simber.tag(USER_ID, true).i(loginManager.getSignedInUserIdOrEmpty())
        Simber.tag(MODULE_IDS, true).i(deviceConfiguration.selectedModules.toString())
        Simber.tag(SUBJECTS_DOWN_SYNC_TRIGGERS, true)
            .i(projectConfiguration.synchronization.frequency.toString())
        Simber.d("[CHECK_LOGIN] Added keys in CrashManager")
    }

    private fun buildAuthorizationEvent(result: AuthorizationResult) =
        AuthorizationEvent(
            timeHelper.now(),
            result,
            if (result == AuthorizationResult.AUTHORIZED) {
                UserInfo(
                    loginManager.getSignedInProjectIdOrEmpty(),
                    loginManager.getSignedInUserIdOrEmpty()
                )
            } else {
                null
            }
        )


    @SuppressLint("CheckResult")
    private suspend fun setSessionIdCrashlyticsKey() {
        ignoreException {
            Simber.tag(SESSION_ID, true).i(eventRepository.getCurrentCaptureSessionEvent().id)
        }
    }
}
