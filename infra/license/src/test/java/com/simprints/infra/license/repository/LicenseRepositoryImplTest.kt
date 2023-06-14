package com.simprints.infra.license.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.LicenseRepositoryImpl
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.LicenseVendor
import com.simprints.infra.license.local.LicenseLocalDataSource
import com.simprints.infra.license.remote.ApiLicenseResult
import com.simprints.infra.license.remote.LicenseRemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.*

class LicenseRepositoryImplTest {
    private val license = UUID.randomUUID().toString()
    private val licenseLocalDataSource: LicenseLocalDataSource = mockk(relaxUnitFun = true)
    private val licenseRemoteDataSource: LicenseRemoteDataSource = mockk()

    private lateinit var licenseRepositoryImpl: LicenseRepositoryImpl

    @Before
    fun setup() {
        coEvery {
            licenseRemoteDataSource.getLicense(
                "invalidProject",
                any(),
                any()
            )
        } returns ApiLicenseResult.Error("001")
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProjectBackendErrorTimed",
                any(),
                any()
            )
        } returns ApiLicenseResult.BackendMaintenanceError(600L)
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProjectBackendError",
                any(),
                any()
            )
        } returns ApiLicenseResult.BackendMaintenanceError()
        coEvery {
            licenseRemoteDataSource.getLicense(
                "validProject",
                any(),
                any()
            )
        } returns ApiLicenseResult.Success(license)

        licenseRepositoryImpl = LicenseRepositoryImpl(
            licenseLocalDataSource,
            licenseRemoteDataSource,
        )
    }

    @Test
    fun `get license flow from local`() = runTest {
        coEvery { licenseLocalDataSource.getLicense() } returns license

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("invalidProject", "deviceId", LicenseVendor.RANK_ONE_FACE)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
    }

    @Test
    fun `get license flow from remote`() = runTest {
        coEvery { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("validProject", "deviceId", LicenseVendor.RANK_ONE_FACE)
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
        coEvery { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("invalidProject", "deviceId", LicenseVendor.RANK_ONE_FACE)
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
        coEvery { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("validProjectBackendErrorTimed", "deviceId", LicenseVendor.RANK_ONE_FACE)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithBackendMaintenanceError(600))
        }
    }

    @Test
    fun `get backend error if things go wrong`() = runTest {
        coEvery { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("validProjectBackendError", "deviceId", LicenseVendor.RANK_ONE_FACE)
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithBackendMaintenanceError(null))
        }
    }
}
