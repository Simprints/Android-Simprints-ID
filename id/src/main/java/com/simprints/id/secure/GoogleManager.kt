package com.simprints.id.secure

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.common.io.BaseEncoding
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GoogleManager {

   fun requestAttestation(ctx: Context, nonce: Nonce): Single<AttestToken> {

       return Single.create<AttestToken> { emitter ->

           SafetyNet.getClient(ctx).attest(BaseEncoding.base64().decode(nonce.value), "AIzaSyAGYfgKYVGHsRJwrPnbNEwLrFfbbNdlAyE")
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
