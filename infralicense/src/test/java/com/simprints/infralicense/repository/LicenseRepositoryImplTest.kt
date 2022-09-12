package com.simprints.infralicense.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.infralicense.local.LicenseLocalDataSource
import com.simprints.infralicense.remote.ApiLicenseResult
import com.simprints.infralicense.remote.LicenseRemoteDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.util.*

class LicenseRepositoryImplTest {
    private val license = UUID.randomUUID().toString()
    private val licenseLocalDataSource: LicenseLocalDataSource = mockk(relaxUnitFun = true)
    private val licenseRemoteDataSource: LicenseRemoteDataSource = mockk()
    private val licenseRepositoryImpl = LicenseRepositoryImpl(licenseLocalDataSource, licenseRemoteDataSource)

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
    }

    @Test
    fun `get license flow from local`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns license

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
    fun `get license flow from remote`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

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
    fun `get error if things go wrong`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

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
    fun `get timed backend error if things go wrong`() = runBlocking {
        every { licenseLocalDataSource.getLicense() } returns null

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
    fun `get backend error if things go wrong`() = runBlocking {
        every { licenseLocalDataSource.getLicense() } returns null

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
