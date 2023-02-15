package com.simprints.infra.login.domain

import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.model.IntegrityErrorCode.*
import com.simprints.core.DispatcherIO
import com.simprints.infra.login.BuildConfig
import com.simprints.infra.login.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class IntegrityTokenRequesterImpl @Inject constructor(
    private val integrityManager: IntegrityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher
) :
    IntegrityTokenRequester {
    /**
     * A method that gets the integrity service token, using a blocking [Tasks.await] method.
     * @param nonce
     * @return Integrity token
     * @throws RequestingIntegrityTokenException
     */

    override suspend fun getToken(nonce: String): String = withContext(dispatcher) {
        try {
            //Wait till the integrity token gets retrieved
            Tasks.await(
                integrityManager.requestIntegrityToken(
                    IntegrityTokenRequest.builder().setNonce(nonce)
                        .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong()).build()
                )
            ).token()
        } catch (integrityServiceException: IntegrityServiceException) {
            throw getException(integrityServiceException.errorCode,integrityServiceException) // Todo Find a better name

        }
    }

    private fun getException(errorCode: Int, cause: Throwable): Throwable =
        when (errorCode) {
            //  errors where the user should update or instLL  play store app
            API_NOT_AVAILABLE, CANNOT_BIND_TO_SERVICE, PLAY_SERVICES_NOT_FOUND, PLAY_SERVICES_VERSION_OUTDATED,
            PLAY_STORE_ACCOUNT_NOT_FOUND,/* This error also means that the google play store app is outdated*/
            PLAY_STORE_NOT_FOUND, PLAY_STORE_VERSION_OUTDATED -> {
                RequestingIntegrityTokenException(
                    errorCode = errorCode,
                    cause = cause
                )
            }
            // errors where the user should retry again later
            GOOGLE_SERVER_UNAVAILABLE, INTERNAL_ERROR -> {
                RequestingIntegrityTokenException(
                    errorCode = errorCode,
                    cause = cause
                )
            }
            // Network errors
            NETWORK_ERROR -> {
                NetworkConnectionException(cause = cause)
            }
            // Non-actionable errors
            // APP_NOT_INSTALLED,APP_UID_MISMATCH, CLOUD_PROJECT_NUMBER_IS_INVALID, NONCE_IS_NOT_BASE64,
            // NONCE_TOO_LONG, NONCE_TOO_SHORT,and  TOO_MANY_REQUESTS,
            else -> {
                RequestingIntegrityTokenException(
                    errorCode = errorCode,
                    cause = cause
                )
            }

        }


}
