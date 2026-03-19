package com.simprints.infra.events.device

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.DeviceConfigurationUpdatedEvent
import com.simprints.infra.events.event.domain.models.DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class DeviceEventTrackerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var eventScope: EventScope

    private lateinit var tracker: DeviceEventTracker

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns NOW
        coEvery { eventRepository.createEventScope(type = EventScopeType.DEVICE) } returns eventScope

        tracker = DeviceEventTracker(
            eventRepository = eventRepository,
            timeHelper = timeHelper,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `trackDeviceConfigurationUpdatedEvent tracks events in a DEVICE event scope`() = runTest {
        val deviceConfig = buildDeviceConfiguration()

        tracker.trackDeviceConfigurationUpdatedEvent(deviceConfig, isLocalChange = false)

        coVerify(exactly = 1) { eventRepository.createEventScope(type = EventScopeType.DEVICE, scopeId = null) }
        coVerify(exactly = 1) { eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED) }
    }

    @Test
    fun `trackDeviceConfigurationUpdatedEvent adds event with correct data`() = runTest {
        val deviceConfig = buildDeviceConfiguration(
            language = "fr",
            selectedModules = listOf<TokenizableString>("module1".asTokenizableRaw()),
        )

        tracker.trackDeviceConfigurationUpdatedEvent(deviceConfig, isLocalChange = false)

        coVerify {
            eventRepository.addOrUpdateEvent(
                eventScope,
                withArg<DeviceConfigurationUpdatedEvent> {
                    assertThat(it.payload.configuration.language).isEqualTo("fr")
                    assertThat(it.payload.configuration.downSyncModules).containsExactly("module1".asTokenizableRaw())
                    assertThat(it.payload.sourceUpdate).isEqualTo(DeviceConfigurationUpdateSource.REMOTE)
                },
            )
        }
    }

    @Test
    fun `trackDeviceConfigurationUpdatedEvent resolves empty module list to null`() = runTest {
        val deviceConfig = buildDeviceConfiguration(
            selectedModules = emptyList(),
        )

        tracker.trackDeviceConfigurationUpdatedEvent(deviceConfig, isLocalChange = true)

        coVerify {
            eventRepository.addOrUpdateEvent(
                eventScope,
                withArg<DeviceConfigurationUpdatedEvent> {
                    assertThat(it.payload.configuration.downSyncModules).isNull()
                    assertThat(it.payload.sourceUpdate).isEqualTo(DeviceConfigurationUpdateSource.LOCAL)
                },
            )
        }
    }

    private fun buildDeviceConfiguration(
        language: String = "en",
        selectedModules: List<TokenizableString> = emptyList(),
    ) = DeviceConfiguration(
        language = language,
        selectedModules = selectedModules,
        lastInstructionId = "",
    )

    companion object {
        private val NOW = Timestamp(1000L)
    }
}
