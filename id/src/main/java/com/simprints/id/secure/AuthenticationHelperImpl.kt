package com.simprints.id.secure

import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.AuthenticateDataResult
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.toDomainResult
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
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
    ): AuthenticateDataResult {
        val result = try {
            logMessageForCrashReportWithNetworkTrigger("Making authentication request")
            loginInfoManager.cleanCredentials()

            loginStartTime = timeHelper.now()
            val nonceScope = NonceScope(projectId, userId)
            projectAuthenticator.authenticate(nonceScope, projectSecret, deviceId)

            logMessageForCrashReportWithNetworkTrigger("Sign in success")
            AuthenticateDataResult.Authenticated
        } catch (t: Throwable) {
            Simber.e(t)

            extractResultFromException(t).also { signInResult ->
                logMessageForCrashReportWithNetworkTrigger("Sign in reason - $signInResult")
            }
        }

        return result.also {
            addEventAndUpdateProjectIdIfRequired(it.toDomainResult(), projectId, userId)
        }
    }

    private fun extractResultFromException(t: Throwable): AuthenticateDataResult {
        return when (t) {
            is IOException -> AuthenticateDataResult.Offline
            is AuthRequestInvalidCredentialsException -> AuthenticateDataResult.BadCredentials
            is SyncCloudIntegrationException -> AuthenticateDataResult.TechnicalFailure
            is BackendMaintenanceException -> {
                AuthenticateDataResult.BackendMaintenanceError(t.estimatedOutage)
            }
            is SafetyNetException -> getSafetyNetExceptionReason(t.reason)
            else -> AuthenticateDataResult.Unknown
        }
    }

    private fun getSafetyNetExceptionReason(
        reason: SafetyNetExceptionReason
    ): AuthenticateDataResult {
        return when (reason) {
            SafetyNetExceptionReason.SERVICE_UNAVAILABLE -> AuthenticateDataResult.SafetyNetUnavailable
            SafetyNetExceptionReason.INVALID_CLAIMS -> AuthenticateDataResult.SafetyNetInvalidClaim
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
