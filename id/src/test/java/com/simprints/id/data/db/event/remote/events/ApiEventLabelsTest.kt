package com.simprints.id.data.db.event.remote.events

import com.google.common.truth.Truth
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.event.remote.models.ApiEventLabels
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import org.junit.Test

class ApiEventLabelsTest {

    @Test
    fun create_ApiEventLabels() {
        val apiEventLabels = ApiEventLabels().apply {
            this["projectId"] = listOf(DEFAULT_PROJECT_ID)
            this["subjectId"] = listOf(SOME_GUID1)
            this["attendantId"] = listOf(DEFAULT_USER_ID)
            this["sessionId"] = listOf(SOME_GUID1)
            this["moduleId"] = listOf(DEFAULT_MODULE_ID)
            this["mode"] = listOf(FINGERPRINT, FACE).map { it.name }
            this["deviceId"] = listOf(SOME_GUID2)
        }

        Truth.assertThat(apiEventLabels).isEqualTo(apiEventLabels.fromApiToDomain().fromDomainToApi())
    }
}
