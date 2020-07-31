package com.simprints.id.data.db.event.remote.models.callback

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.event.local.models.createEnrolmentCallbackEvent
import com.simprints.id.data.db.event.remote.models.ApiEvent
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import org.junit.Test

class ApiCallbackPayloadTest {

    @Test
    fun convertEnrolmentRecordCreationDomain_toApi() {
        val event = createEnrolmentCallbackEvent()
        val newEvent = ApiEvent(event.id, event.labels.fromDomainToApi(), event.payload.fromDomainToApi())

        assertThat(event).isEqualTo(newEvent)
    }
}
