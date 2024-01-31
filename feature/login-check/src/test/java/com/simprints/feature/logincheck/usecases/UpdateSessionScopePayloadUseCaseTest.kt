package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class UpdateSessionScopePayloadUseCaseTest {

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var useCase: UpdateSessionScopePayloadUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = UpdateSessionScopePayloadUseCase(
            eventRepository,
            enrolmentRecordRepository,
            configRepository
        )
    }

    @Test
    fun `Updates current scope with data from enrolments`() = runTest {
        coEvery { enrolmentRecordRepository.count() } returns 42
        coEvery { configRepository.getProjectConfiguration().updatedAt } returns "configUpdatedAt"

        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(withArg {
                assertThat(it.payload.databaseInfo.recordCount).isEqualTo(42)
                assertThat(it.payload.projectConfigurationUpdatedAt).isEqualTo("configUpdatedAt")
            })
        }
    }

    private fun createBlankSessionScope() = SessionScope(
        id = "eventId",
        projectId = "projectId",
        createdAt = Timestamp(0L),
        endedAt = null,
        payload = SessionScopePayload(
            endCause = null,
            modalities = emptyList(),
            sidVersion = "appVersionName",
            libSimprintsVersion = "libVersionName",
            language = "language",
            device = Device("deviceId", "deviceModel", "deviceManufacturer"),
            databaseInfo = DatabaseInfo(0, 0),
            projectConfigurationUpdatedAt = "",
            location = null,
        ),
    )
}
