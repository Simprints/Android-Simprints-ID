package com.simprints.infra.eventsync.event.remote.models.session

import com.google.common.truth.Truth
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import org.junit.Test

class ApiEventScopeTest {

    @Test
    fun `projectConfigurationUpdatedAt is not included in JSON when empty`() {
        // Arrange
        val apiEventScope = ApiEventScope(
            id = "testId",
            projectId = "testProjectId",
            startTime = ApiTimestamp(0),
            endTime = null,
            endCause = ApiEventScopeEndCause.WORKFLOW_ENDED,
            modalities = emptyList(),
            sidVersion = "testSidVersion",
            libSimprintsVersion = "testLibSimprintsVersion",
            language = "testLanguage",
            device = ApiDevice("testDeviceId", "testDeviceModel"),
            databaseInfo = ApiDatabaseInfo(0, 0),
            location = null,
            projectConfigurationUpdatedAt = "",
            projectConfigurationId = "",
            events = emptyList()
        )

        // Act
        val json = JsonHelper.toJson(apiEventScope)

        // Assert
        Truth.assertThat(json).doesNotContain("projectConfigurationUpdatedAt")
    }

    @Test
    fun `projectConfigurationUpdatedAt is included in JSON when not empty`() {
        // Arrange
        val apiEventScope = ApiEventScope(
            id = "testId",
            projectId = "testProjectId",
            startTime = ApiTimestamp(0),
            endTime = null,
            endCause = ApiEventScopeEndCause.WORKFLOW_ENDED,
            modalities = emptyList(),
            sidVersion = "testSidVersion",
            libSimprintsVersion = "testLibSimprintsVersion",
            language = "testLanguage",
            device = ApiDevice("testDeviceId", "testDeviceModel"),
            databaseInfo = ApiDatabaseInfo(0, 0),
            location = null,
            projectConfigurationUpdatedAt = "123",
            projectConfigurationId = "",
            events = emptyList()
        )

        // Act
        val json = JsonHelper.toJson(apiEventScope)

        // Assert
        Truth.assertThat(json).contains("projectConfigurationUpdatedAt")
    }
}
