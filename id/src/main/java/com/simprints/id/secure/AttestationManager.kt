package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.Nonce

interface AttestationManager {

    fun requestAttestation(safetyNetClient: SafetyNetClient, nonce: Nonce): AttestToken
}
