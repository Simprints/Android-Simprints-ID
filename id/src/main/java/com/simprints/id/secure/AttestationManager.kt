package com.simprints.id.secure

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AttestationManager {

   fun requestAttestation(ctx: Context, nonce: Nonce): Single<AttestToken> {

       return Single.create<AttestToken> { emitter ->
           // emitter.onSuccess(AttestToken(""))

           SafetyNet.getClient(ctx).attest(nonce.value.toByteArray(), "AIzaSyAGYfgKYVGHsRJwrPnbNEwLrFfbbNdlAyE")
               .addOnSuccessListener { attestationResponse ->
                   val result = attestationResponse.jwsResult
                   val attestToken = AttestToken(result)
                   emitter.onSuccess(attestToken)
               }
               .addOnFailureListener { e ->
                   print(e)
                   emitter.onError(e)
               }
       }
   }
}
