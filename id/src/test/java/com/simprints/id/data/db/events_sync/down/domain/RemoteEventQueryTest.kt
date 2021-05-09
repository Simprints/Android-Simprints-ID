package com.simprints.id.data.db.events_sync.down.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULES
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.sampledata.DefaultTestConstants.GUID1
import com.simprints.id.sampledata.DefaultTestConstants.GUID2
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.remote.fromDomainToApi
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
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
            listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_MOVE, ENROLMENT_RECORD_DELETION)
        ).fromDomainToApi()

        with(api) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(moduleIds).isEqualTo(DEFAULT_MODULES)
            assertThat(subjectId).isEqualTo(GUID1)
            assertThat(lastEventId).isEqualTo(GUID2)
            assertThat(modes).isEqualTo(DEFAULT_MODES.map { it.fromDomainToApi() })
            assertThat(types).isEqualTo(listOf(EnrolmentRecordCreation, EnrolmentRecordMove, EnrolmentRecordDeletion))
        }
    }
}
