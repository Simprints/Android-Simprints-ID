package com.simprints.id.data.db.event.remote.events.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createEnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.remote.events.fromApiToDomain
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordCreationEventTest {

    @Test
    fun convert_EnrolmentRecordCreationEvent() {
        val original = createEnrolmentRecordCreationEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
