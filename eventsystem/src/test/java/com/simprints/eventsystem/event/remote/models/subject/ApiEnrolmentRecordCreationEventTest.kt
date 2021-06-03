package com.simprints.eventsystem.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.remote.models.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import com.simprints.eventsystem.sampledata.createEnrolmentRecordCreationEvent
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import org.junit.Test

class ApiEnrolmentRecordCreationEventTest {

    @Test
    fun convert_EnrolmentRecordCreationEvent() {
        val original = createEnrolmentRecordCreationEvent(EncodingUtilsImplForTests)
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
