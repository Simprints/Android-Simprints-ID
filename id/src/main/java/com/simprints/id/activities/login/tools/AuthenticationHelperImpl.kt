package com.simprints.id.activities.login.tools

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent.Result.*
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.SafetyNetException
import com.simprints.id.exceptions.safe.secure.SafetyNetExceptionReason
import timber.log.Timber
import java.io.IOException

class AuthenticationHelperImpl(
    private val crashReportManager: CrashReportManager,
    private val loginInfoManager: LoginInfoManager
) : AuthenticationHelper {

    override suspend fun authenticateSafely(
        authBlock: suspend () -> Unit,
        after: (result: AuthenticationEvent.Result) -> Unit
    ): AuthenticationEvent.Result {
        val result = try {
            logMessageForCrashReportWithNetworkTrigger("Making authentication request")
            loginInfoManager.cleanCredentials()

            authBlock.invoke()

            logMessageForCrashReportWithNetworkTrigger("Sign in success")
            AUTHENTICATED
        } catch (t: Throwable) {
            Timber.e(t)
            extractResultFromException(t).also { signInResult ->
                logMessageForCrashReportWithNetworkTrigger("Sign in reason - $signInResult")
            }
        }

        return result.also(after)
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

}
