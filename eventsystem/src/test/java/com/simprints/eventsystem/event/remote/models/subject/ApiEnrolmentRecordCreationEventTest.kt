package com.simprints.eventsystem.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.events.createEnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.remote.models.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordCreationEventTest {

    @Test
    fun convert_EnrolmentRecordCreationEvent() {
        val original = createEnrolmentRecordCreationEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
