package com.simprints.infra.license

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.LicenseCheckEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.license.models.Vendor
import javax.inject.Inject

class SaveLicenseCheckEventUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        vendor: Vendor,
        status: LicenseStatus,
    ) {
        val licenseCheckEvent = LicenseCheckEvent(timeHelper.now(), status.toEventStatus(), vendor.value)
        eventRepository.addOrUpdateEvent(licenseCheckEvent)
    }

    private fun LicenseStatus.toEventStatus() = when (this) {
        LicenseStatus.VALID -> LicenseCheckEvent.LicenseStatus.VALID
        LicenseStatus.INVALID -> LicenseCheckEvent.LicenseStatus.INVALID
        LicenseStatus.EXPIRED -> LicenseCheckEvent.LicenseStatus.EXPIRED
        LicenseStatus.MISSING -> LicenseCheckEvent.LicenseStatus.MISSING
        LicenseStatus.ERROR -> LicenseCheckEvent.LicenseStatus.ERROR
    }
}

/**
 * Represents the status of a license.
 */
enum class LicenseStatus {
    VALID,
    INVALID,
    EXPIRED,
    MISSING,
    ERROR,
}
