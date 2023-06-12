package com.simprints.id.domain.moduleapi.app.responses.entities

import com.google.common.truth.Truth
import com.simprints.id.exitformhandler.ExitFormReason
import org.junit.Test


class RefusalFormReasonTest {

    @Test
    fun `maps domain to module reason`() {
        mapOf(
            ExitFormReason.REFUSED_RELIGION to RefusalFormReason.REFUSED_RELIGION,
            ExitFormReason.REFUSED_DATA_CONCERNS to RefusalFormReason.REFUSED_DATA_CONCERNS,
            ExitFormReason.REFUSED_PERMISSION to RefusalFormReason.REFUSED_PERMISSION,
            ExitFormReason.SCANNER_NOT_WORKING to RefusalFormReason.SCANNER_NOT_WORKING,
            ExitFormReason.APP_NOT_WORKING to RefusalFormReason.APP_NOT_WORKING,
            ExitFormReason.REFUSED_NOT_PRESENT to RefusalFormReason.REFUSED_NOT_PRESENT,
            ExitFormReason.REFUSED_YOUNG to RefusalFormReason.REFUSED_YOUNG,
            ExitFormReason.OTHER to RefusalFormReason.OTHER,
        ).forEach { (domainReason, moduleReason) ->
            Truth.assertThat(domainReason.fromDomainToModuleApi()).isEqualTo(moduleReason)
        }
    }
}
