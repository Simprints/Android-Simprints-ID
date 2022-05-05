package com.simprints.id.services.sync.events.up

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.eventsystem.sampledata.createPersonCreationEvent
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.domain.canSyncBiometricDataToSimprints
import com.simprints.testtools.common.syntax.mock
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventUpSyncHelperImplTest {

    private val operation = SampleDefaults.projectUpSyncScope.operation
    private lateinit var uploadEventsChannel: Channel<Event>

    private lateinit var eventUpSyncHelper: EventUpSyncHelper

    private lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository

    @MockK
    private lateinit var eventUpSyncScopeRepository: com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var settingsPreferencesManager: SettingsPreferencesManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventRepository = EventRepositoryImpl(
            deviceId = "",
            appVersionName = "",
            loginInfoManager = mock(),
            eventLocalDataSource =mock(),
            eventRemoteDataSource =mock(),
            timeHelper =mock(),
            validatorsFactory =mock(),
            libSimprintsVersionName = "",
            sessionDataCache =mock(),
            language = "",
            modalities = listOf()
        )

        eventUpSyncHelper = EventUpSyncHelperImpl(
            eventRepository,
            eventUpSyncScopeRepository,
            timeHelper,
            settingsPreferencesManager
        )

        runBlockingTest {
            mockProgressEmission(emptyFlow())
        }
    }

    @Test
    fun countForUpSync_shouldInvokeTheEventRepo() {
        runBlockingTest {
            eventUpSyncHelper.countForUpSync(operation)

            coVerify { eventRepository.localCount(operation.projectId) }
        }
    }

    @Test
    fun upSync_shouldConsumeRepoEventChannel() {
        runBlocking {
            eventUpSyncHelper.upSync(this, operation).toList()

            coVerify {
                eventRepository.uploadEvents(
                    operation.projectId,
                    any(), any(), any()
                )
            }
        }
    }

    @Test
    fun upSync_shouldProgressEventsInBatches() {
        runBlocking {
            val eventsToUpload = mutableListOf<Event>()
            repeat(3) { eventsToUpload += createPersonCreationEvent() }
            val sequenceOfProgress = flowOf(1, 1, 1)
            mockProgressEmission(sequenceOfProgress)

            val channel = eventUpSyncHelper.upSync(this, operation)

            val progress = channel.toList()
            sequenceOfProgress.onEach {
                assertThat(progress[it].progress).isEqualTo(it)
                assertThat(progress[it].operation.lastState).isEqualTo(RUNNING)
            }

            assertThat(progress.last().operation.lastState).isEqualTo(COMPLETE)
            coVerify(exactly = 4) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
        }
    }

    @Test
    fun upSync_shouldEmitAFailureIfUploadFails() {
        runBlocking {
            coEvery {
                eventRepository.uploadEvents(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } throws Throwable("IO Exception")

            val channel = eventUpSyncHelper.upSync(this, operation)

            val progress = channel.toList()
            assertThat(progress.first().operation.lastState).isEqualTo(FAILED)
            coVerify(exactly = 1) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
        }
    }

    private suspend fun mockProgressEmission(sequence: Flow<Int>) {
        uploadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRepository.uploadEvents(any(), any(), any(), any()) } returns sequence
    }
}
