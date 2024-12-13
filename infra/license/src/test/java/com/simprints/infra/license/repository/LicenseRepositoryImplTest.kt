package com.simprints.infra.license.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.LicenseRepositoryImpl
import com.simprints.infra.license.local.LicenseLocalDataSource
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.license.remote.ApiLicenseResult
import com.simprints.infra.license.remote.LicenseRemoteDataSource
import com.simprints.infra.license.remote.LicenseValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LicenseRepositoryImplTest {
    private val license = License("2023.12.31", "d9f57992-42a2-4b73-b5c8-04f6daa2435c", LicenseVersion("1.0"))
    private val licenseValue = LicenseValue("2023.12.31", "d9f57992-42a2-4b73-b5c8-04f6daa2435c", "1.0")
    private val licenseLocalDataSource: LicenseLocalDataSource = mockk(relaxUnitFun = true)
    private val licenseRemoteDataSource: LicenseRemoteDataSource = mockk()

    private lateinit var licenseRepositoryImpl: LicenseRepositoryImpl

    @Before
    fun setup() {
        coEvery {
            licenseRemoteDataSource.getLicense(
                "invalidProject",
                any(),
                any(),
                any(),
            )
        } returns ApiLicenseResult.Error("001")
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProjectBackendErrorTimed",
                any(),
                any(),
                any(),
            )
        } returns ApiLicenseResult.BackendMaintenanceError(600L)
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProjectBackendError",
                any(),
                any(),
                any(),
            )
        } returns ApiLicenseResult.BackendMaintenanceError()
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProject",
                any(),
                any(),
                any(),
            )
        } returns ApiLicenseResult.Success(licenseValue)

        licenseRepositoryImpl = LicenseRepositoryImpl(
            licenseLocalDataSource,
            licenseRemoteDataSource,
        )
    }

    @Test
    fun `get license flow from local`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns license

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .getLicenseStates("invalidProject", "deviceId", RANK_ONE_FACE, LicenseVersion.UNLIMITED)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
    }

    @Test
    fun `re-download license flow `() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .redownloadLicence("validProject", "deviceId", RANK_ONE_FACE, LicenseVersion.UNLIMITED)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
        coVerify { licenseLocalDataSource.deleteCachedLicense(any()) }
    }

    @Test
    fun `get error if re-downloading go wrong`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .redownloadLicence("invalidProject", "deviceId", RANK_ONE_FACE, LicenseVersion.UNLIMITED)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithError("001"))
        }
        coVerify { licenseLocalDataSource.deleteCachedLicense(any()) }
    }

    @Test
    fun `get license flow from remote`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .getLicenseStates("validProject", "deviceId", RANK_ONE_FACE, LicenseVersion.UNLIMITED)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
    }

    @Test
    fun `get error if things go wrong`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .getLicenseStates("invalidProject", "deviceId", RANK_ONE_FACE, LicenseVersion.UNLIMITED)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithError("001"))
        }
    }

    @Test
    fun `get timed backend error if things go wrong`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .getLicenseStates(
                "validProjectBackendErrorTimed",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            ).toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithBackendMaintenanceError(600))
        }
    }

    @Test
    fun `get backend error if things go wrong`() = runTest {
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl
            .getLicenseStates(
                "validProjectBackendError",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            ).toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithBackendMaintenanceError(null))
        }
    }

    @Test
    fun `test getCachedLicense success`() = runTest {
        // Given
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns license
        // When
        val cachedLicense = licenseRepositoryImpl.getCachedLicense(RANK_ONE_FACE)
        // Then
        assertThat(cachedLicense).isEqualTo(license)
    }

    @Test
    fun `test getCachedLicense failure`() = runTest {
        // Given
        coEvery { licenseLocalDataSource.getLicense(RANK_ONE_FACE) } returns null
        // When
        val license = licenseRepositoryImpl.getCachedLicense(RANK_ONE_FACE)
        // Then
        assertThat(license).isNull()
    }

    @Test
    fun `deletes cached licence`() = runTest {
        licenseRepositoryImpl.deleteCachedLicense(RANK_ONE_FACE)

        coVerify { licenseLocalDataSource.deleteCachedLicense(RANK_ONE_FACE) }
    }

    @Test
    fun `deletes all cached licence`() = runTest {
        licenseRepositoryImpl.deleteCachedLicenses()

        coVerify { licenseLocalDataSource.deleteCachedLicenses() }
    }

    companion object {
        private val RANK_ONE_FACE = Vendor.RankOne
    }
}
