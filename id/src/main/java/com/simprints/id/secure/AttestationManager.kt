package com.simprints.id.secure

//import com.google.common.io.BaseEncoding

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.android.gms.tasks.Tasks
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.ashdavies.rx.rxtasks.toSingle
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalArgumentException

class AttestationManager {

    fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): Single<AttestToken> =
        Single.fromCallable<AttestToken> {
            val result = Tasks.await(safetyNetClient.attest(Base64.decode(nonce.value, NO_WRAP), BuildConfig.ANDROID_AUTH_API_KEY))
            result?.let {
                AttestToken(it.jwsResult)
            } ?: throw IllegalArgumentException("No result from SafetyNet") //TODO: Exception
        }
}

