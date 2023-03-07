package com.simprints.infra.events.event.remote.events

import com.google.common.truth.Truth
import com.simprints.infra.events.remote.models.ApiEventLabels
import com.simprints.infra.events.remote.models.fromApiToDomain
import com.simprints.infra.events.remote.models.fromDomainToApi
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import org.junit.Test

class ApiEventLabelsTest {

    @Test
    fun create_ApiEventLabels() {
        val apiEventLabels = ApiEventLabels().apply {
            this["projectId"] = listOf(DEFAULT_PROJECT_ID)
            this["sessionId"] = listOf(GUID1)
            this["deviceId"] = listOf(GUID2)
        }

        Truth.assertThat(apiEventLabels).isEqualTo(apiEventLabels.fromApiToDomain().fromDomainToApi())
    }
}
