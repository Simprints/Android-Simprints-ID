package com.simprints.infra.eventsync.event.usecases

import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.event.remote.models.ApiEvent
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


internal class MapDomainEventScopeToApiUseCaseTest {
    @MockK
    lateinit var mapDomainEventToApiUseCase: MapDomainEventToApiUseCase

    @MockK
    lateinit var project: Project

    private lateinit var useCase: MapDomainEventScopeToApiUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = MapDomainEventScopeToApiUseCase(mapDomainEventToApiUseCase)
    }

    @Test
    fun `should map events using use case`() {
        val scope = createBlankSessionScope()
        val events = listOf(
            mockk<Event>()
        )
        val resultEvent = mockk<ApiEvent>()
        every { mapDomainEventToApiUseCase(any(), any()) } returns resultEvent

        val result = useCase(scope, events, project)
        verify { mapDomainEventToApiUseCase(events.first(), project) }
        assertEquals(result.events.first(), resultEvent)
    }

    @Test
    fun `should map fields correctly`() {
        val scope = createBlankSessionScope()
        val events = listOf(
            mockk<Event>()
        )
        val resultEvent = mockk<ApiEvent>()
        every { mapDomainEventToApiUseCase(any(), any()) } returns resultEvent

        with(useCase(scope, events, project)) {
            assertEquals(id, scope.id)
            assertEquals(projectId, scope.projectId)
            assertEquals(startTime.unixMs, scope.createdAt.ms)
            assertEquals(endTime, scope.endedAt)
        }

    }

    private fun createBlankSessionScope() = EventScope(
        id = "eventId",
        projectId = "projectId",
        type = EventScopeType.SESSION,
        createdAt = Timestamp(0L),
        endedAt = null,
        payload = EventScopePayload(
            endCause = null,
            modalities = emptyList(),
            sidVersion = "appVersionName",
            libSimprintsVersion = "libVersionName",
            language = "language",
            device = Device("deviceId", "deviceModel", "deviceManufacturer"),
            databaseInfo = DatabaseInfo(0, 0),
            projectConfigurationUpdatedAt = "",
            projectConfigurationId = "",
            location = null,
        ),
    )
}
