package com.simprints.feature.troubleshooting.overview.usecase

import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.models.Vendor
import javax.inject.Inject

internal class CollectLicenceStatesUseCase @Inject constructor(
    private val licenseRepository: LicenseRepository,
) {
    suspend operator fun invoke(): String = Vendor
        .listAll()
        .map { it to licenseRepository.getCachedLicense(it) }
        .filter { it.second != null }
        .joinToString(
            separator = "\n\n",
        ) { (vendor, expiration) -> "${vendor.value} ${expiration?.version?.value}\n- Expires on ${expiration?.expiration}" }
}
