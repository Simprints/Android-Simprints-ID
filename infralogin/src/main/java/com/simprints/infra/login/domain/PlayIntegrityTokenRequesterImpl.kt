package com.simprints.infra.login.domain

import android.util.Base64
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.simprints.infra.login.BuildConfig
import com.simprints.infra.login.exceptions.PlayIntegrityException
import com.simprints.infra.login.exceptions.PlayIntegrityException.PlayIntegrityExceptionReason.SERVICE_UNAVAILABLE
import javax.inject.Inject

internal class PlayIntegrityTokenRequesterImpl @Inject constructor(private val playIntegrityManager: IntegrityManager) :
    PlayIntegrityTokenRequester {

    override fun getToken(nonce: String): String {
        val result = getPlayIntegrityToken(nonce)

        return result.let {
            it.token()!!
        }
    }

    private fun getPlayIntegrityToken(nonce: String) =
        try {
            Tasks.await(
                playIntegrityManager.requestIntegrityToken(
                    IntegrityTokenRequest
                        .builder()
                        .setNonce(nonce.encodeToBase64())
                        .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                        .build()
                )
            )
        } catch (e: Throwable) {
            throw PlayIntegrityException(reason = SERVICE_UNAVAILABLE)
        }

}

// Encode nonce to base64 hex string
private fun String.encodeToBase64(): String =
    Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)

