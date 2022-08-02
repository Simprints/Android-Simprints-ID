package com.simprints.id.secure

import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import java.io.IOException

class AuthenticationHelperImpl(
    private val loginInfoManager: LoginInfoManager,
    private val timeHelper: TimeHelper,
    private val projectAuthenticator: ProjectAuthenticator,
    private val eventRepository: EventRepository,
    private val idPreferencesManager: IdPreferencesManager
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
            AUTHENTICATED
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
            is IOException -> OFFLINE
            is AuthRequestInvalidCredentialsException -> BAD_CREDENTIALS
            is SyncCloudIntegrationException -> TECHNICAL_FAILURE
            is BackendMaintenanceException -> {
                t.estimatedOutage?.let { outage ->
                    idPreferencesManager.setSharedPreference(PREFS_ESTIMATED_OUTAGE, outage)
                }
                BACKEND_MAINTENANCE_ERROR
            }
            is SafetyNetException -> getSafetyNetExceptionReason(t.reason)
            else -> UNKNOWN
        }
    }

    private fun getSafetyNetExceptionReason(
        reason: SafetyNetExceptionReason
    ): Result {
        return when (reason) {
            SafetyNetExceptionReason.SERVICE_UNAVAILABLE -> SAFETYNET_UNAVAILABLE
            SafetyNetExceptionReason.INVALID_CLAIMS -> SAFETYNET_INVALID_CLAIM
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
    companion object {
        const val PREFS_ESTIMATED_OUTAGE = "PREFS_ESTIMATED_OUTAGE"
    }
}
