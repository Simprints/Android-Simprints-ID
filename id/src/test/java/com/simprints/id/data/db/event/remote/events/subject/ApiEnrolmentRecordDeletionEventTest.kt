package com.simprints.id.data.db.event.remote.models.subject

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createEnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiEnrolmentRecordDeletionEventTest {

    @Test
    fun convert_EnrolmentRecordDeletionEvent() {
        val original = createEnrolmentRecordDeletionEvent()
        val transformed = original.fromDomainToApi().fromApiToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
