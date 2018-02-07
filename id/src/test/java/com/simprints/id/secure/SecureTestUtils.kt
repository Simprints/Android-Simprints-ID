package com.simprints.id.secure

import com.simprints.id.secure.models.AttestToken
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import io.reactivex.Single

fun getMockAttestationManager(): AttestationManager {
    val attestationManager = mock<AttestationManager>()
    whenever(attestationManager.requestAttestation(anyNotNull(), anyNotNull())).thenReturn(Single.just(AttestToken("google_attestation")) )
    return attestationManager
}
