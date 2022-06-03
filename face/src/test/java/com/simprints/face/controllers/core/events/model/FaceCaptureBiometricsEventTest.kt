package com.simprints.face.controllers.core.events.model

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import org.junit.Test

class FaceCaptureBiometricsEventTest {

    @Test
    fun `fromDomainToCore maps correctly`() {
        val domain = FaceCaptureBiometricsEvent(
            startTime = 234234,
            endTime = 0,
            eventFace = FaceCaptureBiometricsEvent.EventFace(
                template = "",
                quality = 2.0f,
                format = FaceTemplateFormat.RANK_ONE_1_23
            ),
            payloadId = "someId"
        )

        val core = domain.fromDomainToCore()

        with(core) {
            assertThat(payload.id).isEqualTo(domain.payloadId)
            assertThat(payload.face.quality).isEqualTo(domain.eventFace.quality)
            assertThat(payload.createdAt).isEqualTo(domain.startTime)
        }
    }
}
