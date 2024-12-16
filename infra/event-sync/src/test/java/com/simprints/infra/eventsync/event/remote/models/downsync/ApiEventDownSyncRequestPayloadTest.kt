package com.simprints.infra.eventsync.event.remote.models.downsync

import com.simprints.infra.config.store.models.TokenKeyType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiEventDownSyncRequestPayloadTest {
    @Test
    fun testGetTokenizedFieldJsonPath() {
        // Arrange
        val payload = ApiEventDownSyncRequestPayload(
            startTime = mockk(),
            endTime = mockk(),
            requestId = "requestId",
            queryParameters = ApiEventDownSyncRequestPayload.ApiQueryParameters(
                moduleId = "moduleId",
                attendantId = "attendantId",
                subjectId = null,
                modes = null,
                lastEventId = null,
            ),
            responseStatus = 200,
            errorType = null,
            msToFirstResponseByte = 1000L,
            eventsRead = 10,
        )

        // Act & Assert
        assertEquals("queryParameters.attendantId", payload.getTokenizedFieldJsonPath(TokenKeyType.AttendantId))
        assertEquals("queryParameters.moduleId", payload.getTokenizedFieldJsonPath(TokenKeyType.ModuleId))
        assertEquals(null, payload.getTokenizedFieldJsonPath(TokenKeyType.Unknown))
    }
}
