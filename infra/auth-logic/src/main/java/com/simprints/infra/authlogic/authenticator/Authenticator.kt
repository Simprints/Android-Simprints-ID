package com.simprints.infra.authlogic.authenticator

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authlogic.integrity.exceptions.IntegrityServiceTemporaryDown
import com.simprints.infra.authlogic.integrity.exceptions.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.authlogic.model.AuthenticateDataResult
import com.simprints.infra.authlogic.model.NonceScope
import com.simprints.infra.authlogic.model.toDomainResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AuthenticationEvent
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import java.io.IOException
import javax.inject.Inject

internal class Authenticator @Inject constructor(
    private val authStore: AuthStore,
    private val timeHelper: TimeHelper,
    private val projectAuthenticator: ProjectAuthenticator,
    private val eventRepository: EventRepository,
) {

    private var loginStartTime = timeHelper.nowTimestamp()

    suspend fun authenticate(
        userId: TokenizableString.Raw,
        projectId: String,
        projectSecret: String,
        deviceId: String,
    ): AuthenticateDataResult {
        val result = try {
            logMessageForCrashReportWithNetworkTrigger("Making authentication request")
            authStore.cleanCredentials()

            loginStartTime = timeHelper.nowTimestamp()
            val nonceScope = NonceScope(projectId, deviceId)
            projectAuthenticator.authenticate(nonceScope, projectSecret)

            logMessageForCrashReportWithNetworkTrigger("Sign in success")
            authStore.signedInUserId = userId

            AuthenticateDataResult.Authenticated
        } catch (t: Throwable) {
            when (t) {
                is NetworkConnectionException -> Simber.i(t)
                else -> Simber.e(t)
            }

            extractResultFromException(t).also { signInResult ->
                logMessageForCrashReportWithNetworkTrigger("Sign in reason - ${signInResult.javaClass.simpleName}")
            }
        }

        return result.also { addEventAndUpdateProjectIdIfRequired(it.toDomainResult(), projectId, userId) }
    }

    private fun extractResultFromException(t: Throwable): AuthenticateDataResult {
        return when (t) {
            is IOException -> AuthenticateDataResult.Offline
            is NetworkConnectionException -> AuthenticateDataResult.Offline
            is AuthRequestInvalidCredentialsException -> AuthenticateDataResult.BadCredentials
            is SyncCloudIntegrationException -> AuthenticateDataResult.TechnicalFailure
            is BackendMaintenanceException -> {
                AuthenticateDataResult.BackendMaintenanceError(t.estimatedOutage)
            }

            is RequestingIntegrityTokenException -> AuthenticateDataResult.IntegrityException
            is MissingOrOutdatedGooglePlayStoreApp -> AuthenticateDataResult.MissingOrOutdatedGooglePlayStoreApp
            is IntegrityServiceTemporaryDown -> AuthenticateDataResult.IntegrityServiceTemporaryDown
            else -> AuthenticateDataResult.Unknown
        }
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        Simber.tag(CrashReportTag.LOGIN.name).i(message)
    }

    private suspend fun addEventAndUpdateProjectIdIfRequired(
        result: AuthenticationEvent.AuthenticationPayload.Result,
        projectId: String,
        userId: TokenizableString.Raw
    ) {
        val event = AuthenticationEvent(
            loginStartTime,
            timeHelper.nowTimestamp(),
            UserInfo(projectId, userId),
            result
        )
        eventRepository.addOrUpdateEvent(event)
    }
}
