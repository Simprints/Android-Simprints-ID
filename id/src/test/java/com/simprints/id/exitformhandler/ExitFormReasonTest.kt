package com.simprints.id.exitformhandler

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.id.exitformhandler.ExitFormReason.APP_NOT_WORKING
import com.simprints.id.exitformhandler.ExitFormReason.Companion.fromExitFormOption
import com.simprints.id.exitformhandler.ExitFormReason.OTHER
import com.simprints.id.exitformhandler.ExitFormReason.REFUSED_DATA_CONCERNS
import com.simprints.id.exitformhandler.ExitFormReason.REFUSED_NOT_PRESENT
import com.simprints.id.exitformhandler.ExitFormReason.REFUSED_PERMISSION
import com.simprints.id.exitformhandler.ExitFormReason.REFUSED_RELIGION
import com.simprints.id.exitformhandler.ExitFormReason.REFUSED_YOUNG
import com.simprints.id.exitformhandler.ExitFormReason.SCANNER_NOT_WORKING
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
