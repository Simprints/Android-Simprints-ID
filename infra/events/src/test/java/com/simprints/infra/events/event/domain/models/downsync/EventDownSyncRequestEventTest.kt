package com.simprints.infra.events.event.domain.models.downsync

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent.QueryParameters
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class EventDownSyncRequestEventTest {
    @Test
    fun `getTokenizableFields returns empty map when attendantId and moduleId are null`() {
        val event = getEventDownSyncRequestEvent(
            attendantId = null,
            moduleId = null,
        )

        val result = event.getTokenizableFields()

        assertEquals(emptyMap<TokenKeyType, TokenizableString>(), result)
    }

    @Test
    fun `getTokenizableFields returns map with AttendantId when only attendantId is not null`() {
        val event = getEventDownSyncRequestEvent(
            attendantId = "attendantId",
            moduleId = null,
        )

        val result = event.getTokenizableFields()

        assertEquals(mapOf(TokenKeyType.AttendantId to TokenizableString.Tokenized("attendantId")), result)
    }

    @Test
    fun `getTokenizableFields returns map with ModuleId when only moduleId is not null`() {
        val event = getEventDownSyncRequestEvent(
            attendantId = null,
            moduleId = "moduleId",
        )

        val result = event.getTokenizableFields()

        assertEquals(mapOf(TokenKeyType.ModuleId to TokenizableString.Tokenized("moduleId")), result)
    }

    @Test
    fun `getTokenizableFields returns map with AttendantId and ModuleId when both are not null`() {
        val event = getEventDownSyncRequestEvent(
            attendantId = "attendantId",
            moduleId = "moduleId",
        )

        val result = event.getTokenizableFields()

        assertEquals(
            mapOf(
                TokenKeyType.AttendantId to TokenizableString.Tokenized("attendantId"),
                TokenKeyType.ModuleId to TokenizableString.Tokenized("moduleId"),
            ),
            result,
        )
    }

    private fun getEventDownSyncRequestEvent(
        attendantId: String? = null,
        moduleId: String? = null,
    ): EventDownSyncRequestEvent = EventDownSyncRequestEvent(
        payload = EventDownSyncRequestEvent.EventDownSyncRequestPayload(
            createdAt = mockk<Timestamp>(),
            endedAt = mockk<Timestamp>(),
            requestId = "requestId",
            queryParameters = QueryParameters(
                moduleId = moduleId,
                attendantId = attendantId,
            ),
            responseStatus = 200,
            errorType = "errorType",
            msToFirstResponseByte = 1000,
            eventsRead = 1,
            eventVersion = 1,
        ),
        type = EventType.EVENT_DOWN_SYNC_REQUEST,
    )
}
