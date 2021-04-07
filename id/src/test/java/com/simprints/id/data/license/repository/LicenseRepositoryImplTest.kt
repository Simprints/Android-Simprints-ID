package com.simprints.id.data.license.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.license.local.LicenseLocalDataSource
import com.simprints.id.data.license.remote.LicenseRemoteDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toCollection
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
        coEvery { licenseRemoteDataSource.getLicense("invalidProject", any()) } returns null
        coEvery { licenseRemoteDataSource.getLicense("validProject", any()) } returns license
    }

    @Test
    fun `Get license flow from local`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns license

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("invalidProject", "deviceId")
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
    }

    @Test
    fun `Get license flow from remote`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("validProject", "deviceId")
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithSuccess(license))
        }
    }

    @Test
    fun `Get null license flow if things go wrong`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseStates("invalidProject", "deviceId")
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithError)
        }
    }
}
