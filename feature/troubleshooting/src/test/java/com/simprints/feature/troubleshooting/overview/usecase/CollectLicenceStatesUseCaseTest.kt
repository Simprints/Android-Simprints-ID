package com.simprints.feature.troubleshooting.overview.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CollectLicenceStatesUseCaseTest {
    @MockK
    private lateinit var licenseRepository: LicenseRepository

    private lateinit var useCase: CollectLicenceStatesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = CollectLicenceStatesUseCase(licenseRepository)
    }

    @Test
    fun `returns empty if no licenses`() = runTest {
        coEvery { licenseRepository.getCachedLicense(any()) } returns null
        val licenseText = useCase()

        coVerify(exactly = 2) { licenseRepository.getCachedLicense(any()) }
        assertThat(licenseText).isEmpty()
    }

    @Test
    fun `sets license state when data collected`() = runTest {
        coEvery { licenseRepository.getCachedLicense(any()) } returns null
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2024", "data", LicenseVersion.UNLIMITED)

        val licenseText = useCase()

        coVerify(exactly = 2) { licenseRepository.getCachedLicense(any()) }
        assertThat(licenseText).isNotEmpty()
    }
}
