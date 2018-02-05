package com.simprints.id.secure

import android.app.Activity
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GoogleManager {

   fun requestAttestation(act: Activity, nonce: Nonce): Single<AttestToken> {

       return Single.create<AttestToken> { emitter ->

           SafetyNet.getClient(act).attest(nonce.value.toByteArray(), "AIzaSyAGYfgKYVGHsRJwrPnbNEwLrFfbbNdlAyE")
               .addOnSuccessListener { attestationResponse ->
                   val result = attestationResponse.jwsResult
                   val attestToken = AttestToken(result)
                   emitter.onSuccess(attestToken)
               }
               .addOnFailureListener { e ->
                   print(e)
                   emitter.onError(e)
               }
       }.subscribeOn(Schedulers.io())
   }
}
