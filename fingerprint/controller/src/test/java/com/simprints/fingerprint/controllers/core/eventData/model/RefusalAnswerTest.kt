package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.core.eventData.model.RefusalAnswer.Companion.fromRefusalFormReason
import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason
import org.junit.Test
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload.Answer as CoreRefusalAnswer


internal class RefusalAnswerTest {

    @Test
    fun `maps form refusal reason to domain correctly`() {
        mapOf(
            RefusalFormReason.REFUSED_RELIGION to RefusalAnswer.REFUSED_RELIGION,
            RefusalFormReason.REFUSED_DATA_CONCERNS to RefusalAnswer.REFUSED_DATA_CONCERNS,
            RefusalFormReason.REFUSED_PERMISSION to RefusalAnswer.REFUSED_PERMISSION,
            RefusalFormReason.APP_NOT_WORKING to RefusalAnswer.APP_NOT_WORKING,
            RefusalFormReason.SCANNER_NOT_WORKING to RefusalAnswer.SCANNER_NOT_WORKING,
            RefusalFormReason.REFUSED_NOT_PRESENT to RefusalAnswer.REFUSED_NOT_PRESENT,
            RefusalFormReason.REFUSED_YOUNG to RefusalAnswer.REFUSED_YOUNG,
            RefusalFormReason.OTHER to RefusalAnswer.OTHER
        ).forEach { (option, reason) ->
            assertThat(fromRefusalFormReason(option)).isEqualTo(reason)
        }
    }

    @Test
    fun `maps domain refusal reason to core correctly`() {
        mapOf(
            RefusalAnswer.REFUSED_RELIGION to CoreRefusalAnswer.REFUSED_RELIGION,
            RefusalAnswer.REFUSED_DATA_CONCERNS to CoreRefusalAnswer.REFUSED_DATA_CONCERNS,
            RefusalAnswer.REFUSED_PERMISSION to CoreRefusalAnswer.REFUSED_PERMISSION,
            RefusalAnswer.APP_NOT_WORKING to CoreRefusalAnswer.APP_NOT_WORKING,
            RefusalAnswer.SCANNER_NOT_WORKING to CoreRefusalAnswer.SCANNER_NOT_WORKING,
            RefusalAnswer.REFUSED_NOT_PRESENT to CoreRefusalAnswer.REFUSED_NOT_PRESENT,
            RefusalAnswer.REFUSED_YOUNG to CoreRefusalAnswer.REFUSED_YOUNG,
            RefusalAnswer.OTHER to CoreRefusalAnswer.OTHER
        ).forEach { (option, reason) ->
            assertThat(option.fromDomainToCore()).isEqualTo(reason)
        }
    }
}
