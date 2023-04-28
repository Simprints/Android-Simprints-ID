package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth
import com.simprints.face.controllers.core.events.model.RefusalAnswer.Companion.fromExitFormOption
import com.simprints.feature.exitform.config.ExitFormOption
import org.junit.Test

class RefusalAnswerTest {

    @Test
    fun `maps option to refusal reason correctly`() {
        mapOf(
            ExitFormOption.ReligiousConcerns to RefusalAnswer.REFUSED_RELIGION,
            ExitFormOption.DataConcerns to RefusalAnswer.REFUSED_DATA_CONCERNS,
            ExitFormOption.NoPermission to RefusalAnswer.REFUSED_PERMISSION,
            ExitFormOption.AppNotWorking to RefusalAnswer.APP_NOT_WORKING,
            ExitFormOption.ScannerNotWorking to RefusalAnswer.APP_NOT_WORKING,
            ExitFormOption.PersonNotPresent to RefusalAnswer.REFUSED_NOT_PRESENT,
            ExitFormOption.TooYoung to RefusalAnswer.REFUSED_YOUNG,
            ExitFormOption.Other to RefusalAnswer.OTHER,
        ).forEach { (option, reason) ->
            Truth.assertThat(fromExitFormOption(option)).isEqualTo(reason)
        }
    }
}
