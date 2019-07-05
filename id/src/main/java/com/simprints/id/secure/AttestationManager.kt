package com.simprints.id.secure

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.Tasks
import com.simprints.id.BuildConfig
import com.simprints.id.exceptions.safe.secure.SafetyNetDownException
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single

class AttestationManager {

    fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): Single<AttestToken> {
        return Single.fromCallable<AttestToken> {

            val result = Tasks.await(safetyNetClient.attest(Base64.decode(nonce.value, NO_WRAP), BuildConfig.ANDROID_AUTH_API_KEY)
                .addOnFailureListener {
                    if (it is ApiException) {
                        throw SafetyNetDownException()
                    } else {
                        throw SafetyNetDownException()
                    }
                })
            result?.let {
                AttestToken(it.jwsResult)
            } ?: throw SafetyNetDownException()
        }
    }

}

