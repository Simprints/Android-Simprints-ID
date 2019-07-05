package com.simprints.id.secure

//import com.google.common.io.BaseEncoding

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.exceptions.safe.secure.SafetyNetDownException
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce
import io.reactivex.Single

class AttestationManager {

    fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): Single<AttestToken> =
         throw SafetyNetDownException()

}

