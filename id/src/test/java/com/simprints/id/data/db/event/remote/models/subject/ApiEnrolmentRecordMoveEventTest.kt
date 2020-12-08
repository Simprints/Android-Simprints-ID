package com.simprints.id.data.db.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.events.createEnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordMoveEventTest {

    @Test
    fun convert_EnrolmentRecordMoveEvent() {
        val original = createEnrolmentRecordMoveEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
