package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent
import org.junit.Test

class ApiInvalidIntentEventTest {

    @Test
    fun convertDomainToApi() {
        val intentAction = InvalidIntentEvent.IntentAction.ODK_REGISTER
        val apiIntentAction = intentAction.fromDomainToApi()
        assertThat(apiIntentAction).isEqualTo("com.simprints.simodkadapter.REGISTER")
    }
}
