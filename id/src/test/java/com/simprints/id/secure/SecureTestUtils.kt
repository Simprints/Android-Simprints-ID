package com.simprints.id.secure

import com.simprints.id.secure.models.AttestToken
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.testframework.common.syntax.mock
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.Single

fun getMockAttestationManager(): AttestationManager {
    val attestationManager = mock<AttestationManager>()
    whenever(attestationManager.requestAttestation(anyNotNull(), anyNotNull())).thenReturn(Single.just(AttestToken("google_attestation")) )
    return attestationManager
}
