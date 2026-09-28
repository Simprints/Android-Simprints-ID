package com.simprints.feature.troubleshooting.overview.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.sync.devicestate.DeviceDataState
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CollectDeviceDataStateUseCaseTest {
    @MockK
    private lateinit var deviceStateDataTracker: DeviceStateDataTracker

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: CollectDeviceDataStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.readableBetweenNowAndTime(any()) } returns READABLE_TIME

        useCase = CollectDeviceDataStateUseCase(deviceStateDataTracker, timeHelper)
    }

    @Test
    fun `result contains every field of a fully populated snapshot`() = runTest {
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns deviceDataState()

        val details = useCase()

        assertThat(details).contains(PROJECT_ID)
        assertThat(details).contains("11")
        assertThat(details).contains("22")
        assertThat(details).contains("33")
        assertThat(details).contains("44")
        assertThat(details).contains(EventScopeType.SESSION.name)
        assertThat(details).contains(READABLE_TIME)
    }

    @Test
    fun `unreadable counts render as unknown and never as zero`() = runTest {
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns deviceDataState(
            recordCount = null,
            pendingEvents = null,
            pendingEnrolments = null,
            pendingSamples = null,
            pendingScopes = null,
        )

        val details = useCase()

        assertThat(details.lines().filter { it.contains("unknown") }).hasSize(5)
        assertThat(details).doesNotContain("0")
    }

    @Test
    fun `a readable zero still renders as zero`() = runTest {
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns deviceDataState(
            recordCount = 0,
            pendingEvents = 0,
            pendingEnrolments = 0,
            pendingSamples = 0,
        )

        val details = useCase()

        assertThat(details).doesNotContain("unknown")
        assertThat(details).contains("Records on device: 0")
    }

    @Test
    fun `never-synced timestamps render as never rather than an epoch date`() = runTest {
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns deviceDataState(
            lastEventSyncAt = null,
            lastSampleSyncAt = null,
        )

        val details = useCase()

        assertThat(details).contains("Last event sync: never")
        assertThat(details).contains("Last sample sync: never")
        assertThat(details).doesNotContain(READABLE_TIME)
    }

    @Test
    fun `signed out snapshot renders without throwing`() = runTest {
        coEvery { deviceStateDataTracker.getCurrentDeviceDataState() } returns deviceDataState(projectId = null)

        val details = useCase()

        assertThat(details).contains("signed out")
    }

    private fun deviceDataState(
        projectId: String? = PROJECT_ID,
        recordCount: Int? = 11,
        pendingScopes: Map<EventScopeType, Int>? = mapOf(EventScopeType.SESSION to 5),
        pendingEvents: Int? = 22,
        pendingEnrolments: Int? = 33,
        pendingSamples: Int? = 44,
        lastEventSyncAt: Long? = 1000L,
        lastSampleSyncAt: Long? = 2000L,
    ) = DeviceDataState(
        projectId = projectId,
        recordCount = recordCount,
        pendingScopes = pendingScopes,
        pendingEvents = pendingEvents,
        pendingEnrolments = pendingEnrolments,
        pendingSamples = pendingSamples,
        lastEventSyncAt = lastEventSyncAt,
        lastSampleSyncAt = lastSampleSyncAt,
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val READABLE_TIME = "2 minutes ago"
    }
}
