package com.simprints.id.data.db.event.remote.events.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createEnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.remote.events.fromApiToDomain
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordDeletionEventTest {

    @Test
    fun convert_EnrolmentRecordDeletionEvent() {
        val original = createEnrolmentRecordDeletionEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
