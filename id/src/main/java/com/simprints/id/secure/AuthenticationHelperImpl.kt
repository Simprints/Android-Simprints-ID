package com.simprints.id.secure

import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.extentions.getEstimatedOutage
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BackendMaintenanceError
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BadCredentials
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.Offline
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.SafetyNetInvalidClaim
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.SafetyNetUnavailable
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.TechnicalFailure
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.Unknown
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.exceptions.safe.BackendMaintenanceException
import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.NonceScope
import com.simprints.logging.Simber
import java.io.IOException

class AuthenticationHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val timeHelper: TimeHelper,
    private val projectAuthenticator: ProjectAuthenticator,
    private val eventRepository: EventRepository
) : AuthenticationHelper {

    private var loginStartTime = 0L

    override suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String
    ): Result {
        val result = try {
            logMessageForCrashReportWithNetworkTrigger("Making authentication request")
            loginInfoManager.cleanCredentials()

            loginStartTime = timeHelper.now()
            val nonceScope = NonceScope(projectId, userId)
            projectAuthenticator.authenticate(nonceScope, projectSecret, deviceId)

            logMessageForCrashReportWithNetworkTrigger("Sign in success")
            Result.Authenticated
        } catch (t: Throwable) {
            Simber.e(t)

            extractResultFromException(t).also { signInResult ->
                logMessageForCrashReportWithNetworkTrigger("Sign in reason - $signInResult")
            }
        }

        return result.also {
            addEventAndUpdateProjectIdIfRequired(it, projectId, userId)
        }
    }

    private fun extractResultFromException(t: Throwable): Result {
        return when (t) {
            is IOException -> Offline
            is AuthRequestInvalidCredentialsException -> BadCredentials
            is SimprintsInternalServerException -> TechnicalFailure
            is BackendMaintenanceException -> BackendMaintenanceError(t.getEstimatedOutage())
            is SafetyNetException -> getSafetyNetExceptionReason(t.reason)
            else -> Unknown
        }
    }

    private fun getSafetyNetExceptionReason(
        reason: SafetyNetExceptionReason
    ): Result {
        return when (reason) {
            SafetyNetExceptionReason.SERVICE_UNAVAILABLE -> SafetyNetUnavailable
            SafetyNetExceptionReason.INVALID_CLAIMS -> SafetyNetInvalidClaim
        }
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        Simber.tag(CrashReportTag.LOGIN.name).i(message)
    }

    private fun addEventAndUpdateProjectIdIfRequired(
        result: Result,
        projectId: String,
        userId: String
    ) {
        val event = AuthenticationEvent(
            loginStartTime,
            timeHelper.now(),
            UserInfo(projectId, userId),
            result
        )
        inBackground { eventRepository.addOrUpdateEvent(event) }
    }
}
