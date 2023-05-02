package com.simprints.id.data.exitform

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.id.data.exitform.ExitFormReason.APP_NOT_WORKING
import com.simprints.id.data.exitform.ExitFormReason.Companion.fromExitFormOption
import com.simprints.id.data.exitform.ExitFormReason.OTHER
import com.simprints.id.data.exitform.ExitFormReason.REFUSED_DATA_CONCERNS
import com.simprints.id.data.exitform.ExitFormReason.REFUSED_NOT_PRESENT
import com.simprints.id.data.exitform.ExitFormReason.REFUSED_PERMISSION
import com.simprints.id.data.exitform.ExitFormReason.REFUSED_RELIGION
import com.simprints.id.data.exitform.ExitFormReason.REFUSED_YOUNG
import com.simprints.id.data.exitform.ExitFormReason.SCANNER_NOT_WORKING
import org.junit.Test

class ExitFormReasonTest {

    @Test
    fun `maps options to correct reason`() {
        mapOf(
            ExitFormOption.ReligiousConcerns to REFUSED_RELIGION,
            ExitFormOption.DataConcerns to REFUSED_DATA_CONCERNS,
            ExitFormOption.NoPermission to REFUSED_PERMISSION,
            ExitFormOption.ScannerNotWorking to SCANNER_NOT_WORKING,
            ExitFormOption.AppNotWorking to APP_NOT_WORKING,
            ExitFormOption.PersonNotPresent to REFUSED_NOT_PRESENT,
            ExitFormOption.TooYoung to REFUSED_YOUNG,
            ExitFormOption.Other to OTHER,
        ).forEach { (option, reason) ->
            assertThat(fromExitFormOption(option)).isEqualTo(reason)
        }
    }
}
