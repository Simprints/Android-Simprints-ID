package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class UpdateSessionScopePayloadUseCaseTest {

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var useCase: UpdateSessionScopePayloadUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = UpdateSessionScopePayloadUseCase(
            eventRepository,
            enrolmentRecordRepository,
            configManager
        )
    }

    @Test
    fun `Updates current scope with data from enrolments`() = runTest {
        coEvery { enrolmentRecordRepository.count() } returns 42
        coEvery { configManager.getProjectConfiguration().updatedAt } returns "configUpdatedAt"

        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(withArg {
                assertThat(it.payload.databaseInfo.recordCount).isEqualTo(42)
                assertThat(it.payload.projectConfigurationUpdatedAt).isEqualTo("configUpdatedAt")
            })
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
