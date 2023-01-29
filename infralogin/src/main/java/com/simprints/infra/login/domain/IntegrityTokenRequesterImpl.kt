package com.simprints.infra.login.domain

import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.simprints.infra.login.BuildConfig
import com.simprints.infra.login.exceptions.RequestingIntegrityTokenException
import javax.inject.Inject

internal class IntegrityTokenRequesterImpl @Inject constructor(private val integrityManager: IntegrityManager) :
    IntegrityTokenRequester {
    /**
     * A method that gets the integrity service token, using a blocking [Tasks.await] method.
     * it is a blocking function and  shouldn't be called from the main thread
     * @param nonce
     * @return Integrity token
     * @throws RequestingIntegrityTokenException
     */

    override fun getToken(nonce: String): String =
        try {
            //Wait till the integrity token gets retrieved
            Tasks.await(
                integrityManager.requestIntegrityToken(
                    IntegrityTokenRequest.builder().setNonce(nonce)
                        .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong()).build()
                )
            ).token()
        } catch (integrityServiceException: IntegrityServiceException) {
            throw RequestingIntegrityTokenException(
                errorCode = integrityServiceException.errorCode,
                cause = integrityServiceException
            )
        }
}
