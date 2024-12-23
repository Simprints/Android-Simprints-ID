package com.simprints.infra.license

import com.google.common.truth.Truth
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.LicenseCheckEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.license.models.Vendor
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveLicenseCheckEventUseCaseTest {
    @RelaxedMockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var now: Timestamp

    private lateinit var saveLicenseCheckEventUseCase: SaveLicenseCheckEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        saveLicenseCheckEventUseCase = SaveLicenseCheckEventUseCase(eventRepository, timeHelper)
        every { timeHelper.now() } returns now
    }

    @Test
    fun `invoke adds LicenseCheckEvent with VALID status to eventRepository`() = runTest {
        val vendor = Vendor.RankOne
        val status = LicenseStatus.VALID

        saveLicenseCheckEventUseCase(vendor, status)

        val expectedEvent =
            LicenseCheckEvent(now, LicenseCheckEvent.LicenseStatus.VALID, vendor.value)
        verifyExpectedEvent(expectedEvent)
    }

    @Test
    fun `invoke adds LicenseCheckEvent with INVALID status to eventRepository`() = runTest {
        val status = LicenseStatus.INVALID
        val vendor = Vendor.RankOne

        saveLicenseCheckEventUseCase(vendor, status)

        val expectedEvent =
            LicenseCheckEvent(now, LicenseCheckEvent.LicenseStatus.INVALID, vendor.value)
        verifyExpectedEvent(expectedEvent)
    }

    private fun verifyExpectedEvent(expectedEvent: LicenseCheckEvent) {
        val slot = slot<LicenseCheckEvent>()
        coVerify { eventRepository.addOrUpdateEvent(capture(slot)) }
        val actualPayload = slot.captured.payload
        val expectedPayload = expectedEvent.payload
        Truth.assertThat(actualPayload.status).isEqualTo(expectedPayload.status)
        Truth.assertThat(actualPayload.vendor).isEqualTo(expectedPayload.vendor)
    }
}
