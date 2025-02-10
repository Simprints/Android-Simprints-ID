package com.simprints.infra.authlogic.integrity

import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.model.IntegrityErrorCode.API_NOT_AVAILABLE
import com.google.android.play.core.integrity.model.IntegrityErrorCode.CANNOT_BIND_TO_SERVICE
import com.google.android.play.core.integrity.model.IntegrityErrorCode.CLIENT_TRANSIENT_ERROR
import com.google.android.play.core.integrity.model.IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE
import com.google.android.play.core.integrity.model.IntegrityErrorCode.INTERNAL_ERROR
import com.google.android.play.core.integrity.model.IntegrityErrorCode.NETWORK_ERROR
import com.google.android.play.core.integrity.model.IntegrityErrorCode.PLAY_STORE_ACCOUNT_NOT_FOUND
import com.google.android.play.core.integrity.model.IntegrityErrorCode.PLAY_STORE_NOT_FOUND
import com.google.android.play.core.integrity.model.IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED
import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.BuildConfig
import com.simprints.infra.authlogic.integrity.exceptions.IntegrityServiceTemporaryDown
import com.simprints.infra.authlogic.integrity.exceptions.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class IntegrityTokenRequester @Inject constructor(
    private val integrityManager: IntegrityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    /**
     * A method that gets the integrity service token, using a blocking [Tasks.await] method.
     * @param nonce
     * @return Integrity token
     * @throws RequestingIntegrityTokenException
     */
    suspend fun getToken(nonce: String): String = withContext(dispatcher) {
        try {
            // Wait till the integrity token gets retrieved
            Tasks
                .await(
                    integrityManager.requestIntegrityToken(
                        IntegrityTokenRequest
                            .builder()
                            .setNonce(nonce)
                            .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                            .build(),
                    ),
                ).token()
        } catch (integrityServiceException: IntegrityServiceException) {
            Simber.e("Integrity token request failed", integrityServiceException, tag = LOGIN)
            throw mapException(
                integrityServiceException,
            )
        }
    }

    private fun mapException(integrityServiceException: IntegrityServiceException): Throwable =
        when (val errorCode = integrityServiceException.errorCode) {
            // errors where the user should install or update play store app
            API_NOT_AVAILABLE, CANNOT_BIND_TO_SERVICE,
            PLAY_STORE_ACCOUNT_NOT_FOUND,
            PLAY_STORE_NOT_FOUND, PLAY_STORE_VERSION_OUTDATED,
            -> {
                MissingOrOutdatedGooglePlayStoreApp(errorCode)
            }
            // errors where the user should retry again later
            GOOGLE_SERVER_UNAVAILABLE, INTERNAL_ERROR, CLIENT_TRANSIENT_ERROR -> {
                IntegrityServiceTemporaryDown(errorCode)
            }
            // Network errors
            NETWORK_ERROR -> {
                NetworkConnectionException(cause = integrityServiceException)
            }
            // Non-actionable errors
            // APP_NOT_INSTALLED,APP_UID_MISMATCH, CLOUD_PROJECT_NUMBER_IS_INVALID, NONCE_IS_NOT_BASE64,
            // NONCE_TOO_LONG, NONCE_TOO_SHORT,and  TOO_MANY_REQUESTS
            // error that should be already caught in earlier steps like
            // PLAY_SERVICES_NOT_FOUND,  PLAY_SERVICES_VERSION_OUTDATED
            else -> {
                RequestingIntegrityTokenException(errorCode)
            }
        }
}
