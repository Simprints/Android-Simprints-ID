package com.simprints.infra.events

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.EventRepositoryImpl.Companion.PROJECT_ID_FOR_NOT_SIGNED_IN
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.domain.validators.EventValidator
import com.simprints.infra.events.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventRepositoryImplTest {

    private lateinit var eventRepo: EventRepository

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var eventLocalDataSource: EventLocalDataSource

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var sessionEventValidatorsFactory: SessionEventValidatorsFactory

    @MockK
    lateinit var eventValidator: EventValidator

    @MockK
    lateinit var configRepository: ConfigRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID
        every { sessionEventValidatorsFactory.build() } returns arrayOf(eventValidator)
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
            }
        }
        coEvery { configRepository.getDeviceConfiguration() } returns mockk {
            every { language } returns LANGUAGE
        }

        eventRepo = EventRepositoryImpl(
            deviceId = DEVICE_ID,
            appVersionName = APP_VERSION_NAME,
            libSimprintsVersionName = LIB_VERSION_NAME,
            authStore = authStore,
            eventLocalDataSource = eventLocalDataSource,
            timeHelper = timeHelper,
            validatorsFactory = sessionEventValidatorsFactory,
            configRepository = configRepository,
        )
    }

    @Test
    fun `create event scope should have the right session count`() = runTest {
        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns emptyList()
        coEvery { eventLocalDataSource.countEventScopes(any()) } returns N_SESSIONS_DB

        eventRepo.createEventScope(EventScopeType.SESSION)

        coVerify {
            eventLocalDataSource.saveEventScope(match {
                it.payload.databaseInfo.sessionCount == N_SESSIONS_DB
            })
        }
    }

    @Test
    fun `create event scope for empty project id`() = runTest {
        every { authStore.signedInProjectId } returns ""

        val eventScope = eventRepo.createEventScope(EventScopeType.SESSION)

        assertThat(eventScope.projectId).isEqualTo(PROJECT_ID_FOR_NOT_SIGNED_IN)
    }

    @Test(expected = DuplicateGuidSelectEventValidatorException::class)
    fun `create event scope report duplicate GUID select EventValidatorExceptionException`() =
        runTest {
            coEvery { eventLocalDataSource.countEventScopes(any()) } returns N_SESSIONS_DB
            coEvery {
                eventLocalDataSource.saveEventScope(any())
            } throws DuplicateGuidSelectEventValidatorException("oops...")
            eventRepo.createEventScope(EventScopeType.SESSION)
        }

    @Test
    fun `create event scope should add a new session event`() = runTest {
        eventRepo.createEventScope(EventScopeType.SESSION)

        coVerify {
            eventLocalDataSource.saveEventScope(match { assertANewSessionCaptureWasAdded(it) })
        }
    }

    @Test
    fun `should delegate event scope fetch`() = runTest {
        eventRepo.getEventScope("scopeId")

        coVerify { eventLocalDataSource.loadEventScope("scopeId") }
    }

    @Test
    fun `should close event scope correctly`() = runTest {
        val scope = createSessionScope("scopeId", isClosed = false)
        val event = createAlertScreenEvent()

        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns listOf(
            event.copy(payload = event.payload.copy(endedAt = Timestamp(5))),
            event.copy(payload = event.payload.copy(endedAt = Timestamp(3))),
            event.copy(payload = event.payload.copy(endedAt = Timestamp(1))),
        )
        eventRepo.closeEventScope(scope, null)

        coVerify {
            eventLocalDataSource.saveEventScope(match {
                assertThat(it.endedAt).isEqualTo(Timestamp(5L))
                true
            })
        }
    }

    @Test
    fun `should delete scope on closing if there are no events`() = runTest {
        val scope = createSessionScope("scopeId", isClosed = false)

        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns emptyList()
        eventRepo.closeEventScope(scope, null)

        coVerify {
            eventLocalDataSource.deleteEventScope("scopeId")
        }
    }

    @Test
    fun `should delete scope and events if project not signed in`() = runTest {
        val scope = createSessionScope("scopeId", isClosed = false, projectId = PROJECT_ID_FOR_NOT_SIGNED_IN)

        eventRepo.closeEventScope(scope, null)

        coVerify {
            eventLocalDataSource.deleteEventScope("scopeId")
            eventLocalDataSource.deleteEventsInScope("scopeId")
        }
    }

    @Test
    fun `add event to current session should add event related to current session into DB`() =
        runTest {
            val scope = createSessionScope("scopeId", isClosed = false)
            val event = createAlertScreenEvent()

            coEvery { eventLocalDataSource.loadEventScope(any()) } returns scope
            coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns listOf(
                event.copy(payload = event.payload.copy(endedAt = Timestamp(5))),
            )
            eventRepo.closeEventScope("scopeId", null)

            coVerify {
                eventLocalDataSource.saveEventScope(match {
                    assertThat(it.endedAt).isEqualTo(Timestamp(5L))
                    true
                })
            }
        }

    @Test
    fun `adding event to should not override existing session id in the event`() = runTest {
        val scope = createSessionScope("scopeId", isClosed = false)
        val event = createAlertScreenEvent().copy(
            scopeId = GUID2
        )

        coEvery { eventLocalDataSource.loadEventScope(any()) } returns scope
        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns listOf(event)

        eventRepo.addOrUpdateEvent(scope, event)

        coVerify { eventLocalDataSource.saveEvent(event.copy(scopeId = GUID2)) }
    }

    @Test
    fun `should close all opened scopes of provided type`() = runTest {
        coEvery {
            eventLocalDataSource.loadOpenedScopes(any())
        } returns listOf(createSessionScope("scopeId"), createSessionScope("scopeId2"))

        val event = createAlertScreenEvent()
        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns listOf(
            event.copy(payload = event.payload.copy(endedAt = Timestamp(5))),
        )

        eventRepo.closeAllOpenScopes(EventScopeType.SESSION, null)

        coVerify(exactly = 2) {
            eventLocalDataSource.saveEventScope(withArg {
                assertThat(it.endedAt).isNotNull()
                assertThat(it.payload.endCause).isNotNull()
            })
        }
    }

    @Test
    fun `should delegate scope saving`() = runTest {
        val scope = createSessionScope("scopeId")
        eventRepo.saveEventScope(scope)

        coVerify { eventLocalDataSource.saveEventScope(any()) }
    }

    @Test
    fun `should delegate open scope fetch`() = runTest {
        eventRepo.getOpenEventScopes(type = EventScopeType.SESSION)

        coVerify { eventLocalDataSource.loadOpenedScopes(EventScopeType.SESSION) }
    }

    @Test
    fun `should delegate closed scope fetch`() = runTest {
        eventRepo.getClosedEventScopes(type = EventScopeType.SESSION, limit = 10)

        coVerify { eventLocalDataSource.loadClosedScopes(EventScopeType.SESSION, limit = 10) }
    }

    @Test
    fun `deleting scope should delete events in scope`() = runTest {
        eventRepo.deleteEventScope("scopeId")

        coVerify {
            eventLocalDataSource.deleteEventScope("scopeId")
            eventLocalDataSource.deleteEventsInScope("scopeId")
        }
    }

    @Test
    fun `deleting scope should delete events in scopes`() = runTest {
        eventRepo.deleteEventScopes(listOf("scopeId"))

        coVerify {
            eventLocalDataSource.deleteEventScopes(listOf("scopeId"))
            eventLocalDataSource.deleteEventsInScopes(listOf("scopeId"))
        }
    }

    @Test
    fun `should delegate event fetch`() = runTest {
        eventRepo.getEventsFromScope("scopeId")

        coVerify { eventLocalDataSource.loadEventsInScope("scopeId") }
    }

    @Test
    fun `should delegate event json fetch`() = runTest {
        eventRepo.getEventsJsonFromScope("scopeId")

        coVerify { eventLocalDataSource.loadEventJsonInScope("scopeId") }
    }

    @Test
    fun `should delegate all fetch`() = runTest {
        eventRepo.getAllEvents()

        coVerify { eventLocalDataSource.loadAllEvents() }
    }

    @Test
    fun `when observeEventCount called with null type return all events`() = runTest {
        coEvery { eventLocalDataSource.observeEventCount() } returns flowOf(7)

        assertThat(eventRepo.observeEventCount(null).firstOrNull()).isEqualTo(7)

        coVerify(exactly = 1) { eventLocalDataSource.observeEventCount() }
        coVerify(exactly = 0) { eventLocalDataSource.observeEventCount(any()) }
    }

    @Test
    fun `when observeEventCount called with type return events of type`() = runTest {
        coEvery { eventLocalDataSource.observeEventCount(any()) } returns flowOf(7)

        assertThat(
            eventRepo.observeEventCount(EventType.CALLBACK_ENROLMENT).firstOrNull()
        ).isEqualTo(7)

        coVerify(exactly = 0) { eventLocalDataSource.observeEventCount() }
        coVerify(exactly = 1) { eventLocalDataSource.observeEventCount(any()) }
    }

    @Test
    fun `insert event into event scope should update event fields`() = runTest {
        val scope = createSessionScope(GUID1)
        val event = createAlertScreenEvent()
        val updatedEvent = eventRepo.addOrUpdateEvent(scope, event, emptyList())

        coVerify {
            eventLocalDataSource.saveEvent(
                withArg {
                    assertThat(it.scopeId).isEqualTo(scope.id)
                    assertThat(it.projectId).isEqualTo(DEFAULT_PROJECT_ID)
                }
            )
        }
        assertThat(updatedEvent.scopeId).isEqualTo(scope.id)
    }

    @Test
    fun `insert event should return updated event`() = runTest {
        val scope = createSessionScope(GUID1)
        val event = createAlertScreenEvent()
        val updatedEvent = eventRepo.addOrUpdateEvent(scope, event, emptyList())

        coVerify { eventLocalDataSource.saveEvent(any()) }
        assertThat(updatedEvent.scopeId).isEqualTo(scope.id)
        assertThat(updatedEvent.projectId).isEqualTo(DEFAULT_PROJECT_ID)
    }

    @Test
    fun `insert event should should check local db if no event list provided`() = runTest {
        coEvery { eventLocalDataSource.loadEventsInScope(any()) } returns emptyList()

        val scope = createSessionScope(GUID1)
        val event = createAlertScreenEvent()
        val updatedEvent = eventRepo.addOrUpdateEvent(scope, event, null)

        coVerify { eventLocalDataSource.saveEvent(any()) }
        assertThat(updatedEvent.scopeId).isEqualTo(scope.id)
        assertThat(updatedEvent.projectId).isEqualTo(DEFAULT_PROJECT_ID)
    }

    @Test
    fun `insert event into current open session should invoke validators`() = runTest {
        val scope = createSessionScope(GUID1)
        val eventInScope = createAlertScreenEvent()
        val newEvent = createAlertScreenEvent()

        eventRepo.addOrUpdateEvent(scope, newEvent, listOf(eventInScope))

        verify { eventValidator.validate(listOf(eventInScope), newEvent) }
    }

    @Test
    fun `should delegate delete all`() = runTest {
        eventRepo.deleteAll()

        coVerify { eventLocalDataSource.deleteAll() }
    }

    private fun assertANewSessionCaptureWasAdded(scope: EventScope): Boolean =
        scope.projectId == DEFAULT_PROJECT_ID &&
            scope.createdAt == NOW &&
            scope.endedAt == null &&
            scope.payload.modalities == listOf(Modality.FINGERPRINT, Modality.FACE) &&
            scope.payload.sidVersion == APP_VERSION_NAME &&
            scope.payload.language == LANGUAGE &&
            scope.payload.device == Device(
            Build.VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            DEVICE_ID
        ) && scope.payload.databaseInfo == DatabaseInfo(0)

    companion object {

        const val DEVICE_ID = "DEVICE_ID"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val LIB_VERSION_NAME = "LIB_VERSION_NAME"
        const val LANGUAGE = "en"

        const val N_SESSIONS_DB = 3
        val NOW = Timestamp(1000L)
    }
}
