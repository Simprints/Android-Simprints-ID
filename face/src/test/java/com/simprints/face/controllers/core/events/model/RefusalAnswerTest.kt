package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth.assertThat
import com.simprints.face.controllers.core.events.model.RefusalAnswer.Companion.fromExitFormOption
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.moduleapi.face.responses.IFaceExitReason
import org.junit.Test

class RefusalAnswerTest {

    @Test
    fun `maps option to refusal reason correctly`() {
        mapOf(
            ExitFormOption.ReligiousConcerns to RefusalAnswer.REFUSED_RELIGION,
            ExitFormOption.DataConcerns to RefusalAnswer.REFUSED_DATA_CONCERNS,
            ExitFormOption.NoPermission to RefusalAnswer.REFUSED_PERMISSION,
            ExitFormOption.AppNotWorking to RefusalAnswer.APP_NOT_WORKING,
            ExitFormOption.ScannerNotWorking to RefusalAnswer.SCANNER_NOT_WORKING,
            ExitFormOption.PersonNotPresent to RefusalAnswer.REFUSED_NOT_PRESENT,
            ExitFormOption.TooYoung to RefusalAnswer.REFUSED_YOUNG,
            ExitFormOption.Other to RefusalAnswer.OTHER,
        ).forEach { (option, reason) ->
            assertThat(fromExitFormOption(option)).isEqualTo(reason)
        }
    }

    @Test
    fun `maps domain option to exit reason correctly`() {
        mapOf(
            RefusalAnswer.REFUSED_RELIGION to IFaceExitReason.REFUSED_RELIGION,
            RefusalAnswer.REFUSED_DATA_CONCERNS to IFaceExitReason.REFUSED_DATA_CONCERNS,
            RefusalAnswer.REFUSED_PERMISSION to IFaceExitReason.REFUSED_PERMISSION,
            RefusalAnswer.APP_NOT_WORKING to IFaceExitReason.APP_NOT_WORKING,
            RefusalAnswer.SCANNER_NOT_WORKING to IFaceExitReason.SCANNER_NOT_WORKING,
            RefusalAnswer.REFUSED_NOT_PRESENT to IFaceExitReason.REFUSED_NOT_PRESENT,
            RefusalAnswer.REFUSED_YOUNG to IFaceExitReason.REFUSED_YOUNG,
            RefusalAnswer.OTHER to IFaceExitReason.OTHER
        ).forEach { (option, reason) ->
            assertThat(option.fromDomainToExitReason()).isEqualTo(reason)
        }
    }
}
