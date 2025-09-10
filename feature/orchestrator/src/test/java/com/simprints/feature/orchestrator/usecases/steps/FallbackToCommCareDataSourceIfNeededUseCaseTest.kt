package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.orchestration.data.ActionRequest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class FallbackToCommCareDataSourceIfNeededUseCaseTest {
    @RelaxedMockK
    private lateinit var eventSyncCache: EventSyncCache

    @RelaxedMockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: FallbackToCommCareDataSourceIfNeededUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FallbackToCommCareDataSourceIfNeededUseCase(eventSyncCache, timeHelper)
    }

    private fun createProjectConfigurationWithCommCare(thresholdDays: Int = 3): ProjectConfiguration {
        val syncConfig = mockk<SynchronizationConfiguration>(relaxed = true)
        val downSyncConfig = mockk<DownSynchronizationConfiguration>(relaxed = true)
        val commCareConfig = DownSynchronizationConfiguration.CommCareDownSynchronizationConfiguration

        every { downSyncConfig.commCare } returns commCareConfig
        every { syncConfig.down } returns downSyncConfig

        return mockk<ProjectConfiguration>(relaxed = true) {
            every { synchronization } returns syncConfig
            every { custom } returns mapOf("fallbackToCommCareThresholdDays" to thresholdDays)
        }
    }

    private fun createProjectConfigurationWithoutCommCare(): ProjectConfiguration {
        val syncConfig = mockk<SynchronizationConfiguration>(relaxed = true)
        val downSyncConfig = mockk<DownSynchronizationConfiguration>(relaxed = true)

        every { downSyncConfig.commCare } returns null
        every { syncConfig.down } returns downSyncConfig

        return mockk<ProjectConfiguration>(relaxed = true) {
            every { synchronization } returns syncConfig
        }
    }

    @Test
    fun `invoke EnrolActionRequest - CommCare data source - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns BiometricDataSource.COMMCARE
        }

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke EnrolActionRequest - no CommCare config - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithoutCommCare()
        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
        }

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke EnrolActionRequest - recent successful sync - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val action = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
        }
        val recentSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1)) // 1 second ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns recentSyncTime
        every { timeHelper.msBetweenNowAndTime(recentSyncTime) } returns TimeUnit.SECONDS.toMillis(1) // 1 second

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke EnrolActionRequest - old sync time - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val oldSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4)) // 4 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns oldSyncTime
        every { timeHelper.msBetweenNowAndTime(oldSyncTime) } returns TimeUnit.DAYS.toMillis(4) // 4 days

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke EnrolActionRequest - no sync time - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns null

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke EnrolActionRequest - sync time exactly at threshold - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val thresholdSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) // Exactly 3 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns thresholdSyncTime
        every { timeHelper.msBetweenNowAndTime(thresholdSyncTime) } returns TimeUnit.DAYS.toMillis(3) // Exactly 3 days

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke IdentifyActionRequest - CommCare data source - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns BiometricDataSource.COMMCARE
        }

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke IdentifyActionRequest - no CommCare config - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithoutCommCare()
        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
        }

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke IdentifyActionRequest - recent successful sync - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
        }
        val recentSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1)) // 1 second ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns recentSyncTime
        every { timeHelper.msBetweenNowAndTime(recentSyncTime) } returns TimeUnit.SECONDS.toMillis(1) // 1 second

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke IdentifyActionRequest - old sync time - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val oldSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4)) // 4 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns oldSyncTime
        every { timeHelper.msBetweenNowAndTime(oldSyncTime) } returns TimeUnit.DAYS.toMillis(4) // 4 days

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke IdentifyActionRequest - no sync time - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns null

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke IdentifyActionRequest - sync time exactly at threshold - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val thresholdSyncTime = Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) // Exactly 3 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns thresholdSyncTime
        every { timeHelper.msBetweenNowAndTime(thresholdSyncTime) } returns TimeUnit.DAYS.toMillis(3) // Exactly 3 days

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke IdentifyActionRequest - sync time just under threshold - returns original action`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val action = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
        }
        val justUnderThresholdSyncTime = Timestamp(System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(3) - 1L)) // Just under 3 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns justUnderThresholdSyncTime
        every { timeHelper.msBetweenNowAndTime(justUnderThresholdSyncTime) } returns TimeUnit.DAYS.toMillis(3) - 1L // Just under 3 days

        val result = useCase(action, projectConfig)

        assertEquals(action, result)
    }

    @Test
    fun `invoke IdentifyActionRequest - sync time just over threshold - falls back to CommCare`() = runTest {
        val projectConfig = createProjectConfigurationWithCommCare()
        val originalAction = mockk<ActionRequest.IdentifyActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val overThresholdSyncTime = Timestamp(System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(3) + 1L)) // Just over 3 days ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns overThresholdSyncTime
        every { timeHelper.msBetweenNowAndTime(overThresholdSyncTime) } returns TimeUnit.DAYS.toMillis(3) + 1L

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }

    @Test
    fun `invoke EnrolActionRequest - custom threshold configuration - respects configured threshold`() = runTest {
        val customThresholdDays = 1
        val projectConfig = createProjectConfigurationWithCommCare(customThresholdDays)
        val originalAction = mockk<ActionRequest.EnrolActionRequest>(relaxed = true) {
            every { biometricDataSource } returns "OTHER_SOURCE"
            every { copy(biometricDataSource = BiometricDataSource.COMMCARE) } returns mockk {
                every { biometricDataSource } returns BiometricDataSource.COMMCARE
            }
        }
        val syncTime = Timestamp(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(25)) // 25 hours ago

        coEvery { eventSyncCache.readLastSuccessfulSyncTime() } returns syncTime
        every { timeHelper.msBetweenNowAndTime(syncTime) } returns TimeUnit.HOURS.toMillis(25) // 25 hours

        val result = useCase(originalAction, projectConfig)

        assertEquals(BiometricDataSource.COMMCARE, result.biometricDataSource)
    }
}
