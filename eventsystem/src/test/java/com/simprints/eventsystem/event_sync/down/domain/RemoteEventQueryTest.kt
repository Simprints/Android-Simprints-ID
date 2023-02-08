package com.simprints.eventsystem.event_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.event.remote.fromDomainToApi
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.events_sync.down.domain.fromDomainToApi
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULES
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import org.junit.Test

class RemoteEventQueryTest {

    @Test
    fun remoteEventQuery_fromDomainToAPi() {
        val api = RemoteEventQuery(
            DEFAULT_PROJECT_ID,
            DEFAULT_USER_ID,
            DEFAULT_MODULES,
            GUID1,
            GUID2,
            DEFAULT_MODES,
        ).fromDomainToApi()

        with(api) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleIds).isEqualTo(DEFAULT_MODULES)
            assertThat(subjectId).isEqualTo(GUID1)
            assertThat(lastEventId).isEqualTo(GUID2)
            assertThat(modes).isEqualTo(DEFAULT_MODES.map { it.fromDomainToApi() })
        }
    }
}
