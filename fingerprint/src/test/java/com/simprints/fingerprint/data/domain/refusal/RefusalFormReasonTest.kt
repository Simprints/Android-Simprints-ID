package com.simprints.fingerprint.data.domain.refusal

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.*
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.Companion.fromExitFormOption

import org.junit.Test

class RefusalFormReasonTest {

    @Test
    fun `maps option to refusal reason correctly`() {
        mapOf(
            ExitFormOption.ReligiousConcerns to REFUSED_RELIGION,
            ExitFormOption.DataConcerns to REFUSED_DATA_CONCERNS,
            ExitFormOption.NoPermission to REFUSED_PERMISSION,
            ExitFormOption.AppNotWorking to SCANNER_NOT_WORKING,
            ExitFormOption.ScannerNotWorking to SCANNER_NOT_WORKING,
            ExitFormOption.PersonNotPresent to REFUSED_NOT_PRESENT,
            ExitFormOption.TooYoung to REFUSED_YOUNG,
            ExitFormOption.Other to OTHER,
        ).forEach { (option, reason) ->
            assertThat(fromExitFormOption(option)).isEqualTo(reason)
        }
    }
}
