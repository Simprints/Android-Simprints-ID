package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class UpdateDatabaseCountsInCurrentSessionUseCaseTest {

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    private lateinit var useCase: UpdateDatabaseCountsInCurrentSessionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = UpdateDatabaseCountsInCurrentSessionUseCase(eventRepository, enrolmentRecordManager)
    }

    @Test
    fun `Updates current event with data from enrolments`() = runTest {
        coEvery { enrolmentRecordManager.count() } returns 42

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createBlankSessionEvent()

        useCase()

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat((it.payload as SessionCaptureEvent.SessionCapturePayload).databaseInfo.recordCount).isEqualTo(42)
            })
        }
    }

    private fun createBlankSessionEvent() = SessionCaptureEvent(
        id = "eventId",
        projectId = "projectId",
        createdAt = 0,
        modalities = emptyList(),
        appVersionName = "appVersionName",
        libVersionName = "libVersionName",
        language = "language",
        device = Device("deviceId", "deviceModel", "deviceManufacturer"),
        databaseInfo = DatabaseInfo(0, 0),
    )
}
