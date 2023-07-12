package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason
import com.simprints.moduleapi.fingerprint.responses.IFingerprintExitReason
import org.junit.Test


internal class FingerprintRefusalFormReasonTest {

    @Test
    fun `maps fingerprint refusal option to app refusal form reason correctly`() {
        mapOf(
            FingerprintRefusalFormReason.REFUSED_RELIGION to RefusalFormReason.REFUSED_RELIGION,
            FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS to RefusalFormReason.REFUSED_DATA_CONCERNS,
            FingerprintRefusalFormReason.REFUSED_PERMISSION to RefusalFormReason.REFUSED_PERMISSION,
            FingerprintRefusalFormReason.APP_NOT_WORKING to RefusalFormReason.APP_NOT_WORKING,
            FingerprintRefusalFormReason.SCANNER_NOT_WORKING to RefusalFormReason.SCANNER_NOT_WORKING,
            FingerprintRefusalFormReason.REFUSED_NOT_PRESENT to RefusalFormReason.REFUSED_NOT_PRESENT,
            FingerprintRefusalFormReason.REFUSED_YOUNG to RefusalFormReason.REFUSED_YOUNG,
            FingerprintRefusalFormReason.OTHER to RefusalFormReason.OTHER
        ).forEach { (option, reason) ->
            assertThat(option.toAppRefusalFormReason()).isEqualTo(reason)
        }
    }

    @Test
    fun `maps fingerprint exit reason to domain correctly`() {
        mapOf(
            IFingerprintExitReason.REFUSED_RELIGION to FingerprintRefusalFormReason.REFUSED_RELIGION,
            IFingerprintExitReason.REFUSED_DATA_CONCERNS to FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS,
            IFingerprintExitReason.REFUSED_PERMISSION to FingerprintRefusalFormReason.REFUSED_PERMISSION,
            IFingerprintExitReason.APP_NOT_WORKING to FingerprintRefusalFormReason.APP_NOT_WORKING,
            IFingerprintExitReason.SCANNER_NOT_WORKING to FingerprintRefusalFormReason.SCANNER_NOT_WORKING,
            IFingerprintExitReason.REFUSED_NOT_PRESENT to FingerprintRefusalFormReason.REFUSED_NOT_PRESENT,
            IFingerprintExitReason.REFUSED_YOUNG to FingerprintRefusalFormReason.REFUSED_YOUNG,
            IFingerprintExitReason.OTHER to FingerprintRefusalFormReason.OTHER
        ).forEach { (option, reason) ->
            assertThat(option.fromModuleApiToDomain()).isEqualTo(reason)
        }
    }
}
