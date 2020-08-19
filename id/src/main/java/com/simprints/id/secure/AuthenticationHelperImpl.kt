package com.simprints.id.secure

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent.Result.*
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.time.TimeHelper
import timber.log.Timber
import java.io.IOException

class AuthenticationHelperImpl(
        private val crashReportManager: CrashReportManager,
        private val loginInfoManager: LoginInfoManager,
        private val timeHelper: TimeHelper,
        private val projectAuthenticator: ProjectAuthenticator,
        private val sessionRepository: SessionRepository
) : AuthenticationHelper {

    private var loginStartTime = 0L

    override suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String
    ): AuthenticationEvent.Result {
        val result = try {
            logMessageForCrashReportWithNetworkTrigger("Making authentication request")
            loginInfoManager.cleanCredentials()

            loginStartTime = timeHelper.now()
            val nonceScope = NonceScope(projectId, userId)
            projectAuthenticator.authenticate(nonceScope, projectSecret, deviceId)

            logMessageForCrashReportWithNetworkTrigger("Sign in success")
            AUTHENTICATED
        } catch (t: Throwable) {
            Timber.e(t)
            crashReportManager.logExceptionOrSafeException(t)

            extractResultFromException(t).also { signInResult ->
                logMessageForCrashReportWithNetworkTrigger("Sign in reason - $signInResult")
            }
        }

        return result.also {
            addEventAndUpdateProjectIdIfRequired(it, projectId, userId)
        }
    }

    private fun extractResultFromException(t: Throwable): AuthenticationEvent.Result {
        return when (t) {
            is IOException -> OFFLINE
            is AuthRequestInvalidCredentialsException -> BAD_CREDENTIALS
            is SimprintsInternalServerException -> TECHNICAL_FAILURE
            is SafetyNetException -> getSafetyNetExceptionReason(t.reason)
            else -> UNKNOWN
        }
    }

    private fun getSafetyNetExceptionReason(
        reason: SafetyNetExceptionReason
    ): AuthenticationEvent.Result {
        return when (reason) {
            SafetyNetExceptionReason.SERVICE_UNAVAILABLE -> SAFETYNET_UNAVAILABLE
            SafetyNetExceptionReason.INVALID_CLAIMS -> SAFETYNET_INVALID_CLAIM
        }
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.LOGIN,
            CrashReportTrigger.NETWORK,
            message = message
        )
    }

    private fun addEventAndUpdateProjectIdIfRequired(
        result: AuthenticationEvent.Result,
        projectId: String,
        userId: String
    ) {
        val event = AuthenticationEvent(
            loginStartTime,
            timeHelper.now(),
            AuthenticationEvent.UserInfo(projectId, userId),
            result
        )
        sessionRepository.addEventToCurrentSessionInBackground(event)
    }
}
