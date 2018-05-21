package com.simprints.id.secure

import com.simprints.id.secure.models.AttestToken
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.whenever
import io.reactivex.Single

fun getMockAttestationManager(): AttestationManager {
    val attestationManager = mock<AttestationManager>()
    whenever(attestationManager.requestAttestation(anyNotNull(), anyNotNull())).thenReturn(Single.just(AttestToken("google_attestation")) )
    return attestationManager
}
