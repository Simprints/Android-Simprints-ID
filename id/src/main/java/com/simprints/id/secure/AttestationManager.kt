package com.simprints.id.secure

//import com.google.common.io.BaseEncoding

import android.util.Base64
import android.util.Base64.NO_WRAP
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.BuildConfig
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AttestationManager {

   fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): Single<AttestToken> {

       return Single.create<AttestToken> { emitter ->

           safetyNetClient.attest(Base64.decode(nonce.value, NO_WRAP), BuildConfig.ANDROID_AUTH_API_KEY)
               .addOnSuccessListener { attestationResponse ->
                   val result = attestationResponse.jwsResult
                   val attestToken = AttestToken(result)
                   emitter.onSuccess(attestToken)
               }
               .addOnFailureListener { e ->
                   print(e)

                   // We go ahead with the auth flow, the server will reject the request.
                   // So we can turn off the Safety check on the server if we need.
                   emitter.onSuccess(AttestToken(""))
               }
       }.subscribeOn(Schedulers.io())
   }
}
