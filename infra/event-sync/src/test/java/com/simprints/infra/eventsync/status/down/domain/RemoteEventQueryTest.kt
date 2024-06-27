package com.simprints.infra.eventsync.status.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class RemoteEventQueryTest {

    @Test
    fun remoteEventQuery_fromDomainToAPi() {
        val api = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            attendantId = DEFAULT_USER_ID.value,
            moduleId = DEFAULT_MODULE_ID.value,
            subjectId = GUID1,
            lastEventId = GUID2,
            modes = DEFAULT_MODES,
        ).fromDomainToApi()

        with(api) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID.value)
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID.value)
            assertThat(subjectId).isEqualTo(GUID1)
            assertThat(lastEventId).isEqualTo(GUID2)
        }
    }
}
