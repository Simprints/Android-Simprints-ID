package com.simprints.id.secure

import com.simprints.id.secure.models.AttestToken
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import io.reactivex.Single

fun getMockGoogleManager(): GoogleManager {
    val googleManager = mock<GoogleManager>()
    whenever(googleManager.requestAttestation(anyNotNull(), anyNotNull())).thenReturn(Single.just(AttestToken("google_attestation")) )
    return googleManager
}
