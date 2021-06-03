package com.simprints.eventsystem.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.remote.models.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import com.simprints.eventsystem.sampledata.createEnrolmentRecordMoveEvent
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import org.junit.Test

class ApiEnrolmentRecordMoveEventTest {

    @Test
    fun convert_EnrolmentRecordMoveEvent() {
        val original = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
