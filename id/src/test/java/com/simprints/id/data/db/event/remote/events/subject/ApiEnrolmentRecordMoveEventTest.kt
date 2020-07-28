package com.simprints.id.data.db.event.remote.events.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createEnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.events.fromApiToDomain
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordMoveEventTest {

    @Test
    fun convert_EnrolmentRecordMoveEvent() {
        val original = createEnrolmentRecordMoveEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
