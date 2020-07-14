package com.simprints.face.license.data.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.face.license.data.local.LicenseLocalDataSource
import com.simprints.face.license.data.remote.LicenseRemoteDataSource
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class LicenseRepositoryImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }
    private val license = UUID.randomUUID().toString()
    private val licenseLocalDataSource: LicenseLocalDataSource = mockk(relaxUnitFun = true)
    private val licenseRemoteDataSource: LicenseRemoteDataSource = mockk()
    private val licenseRepositoryImpl =
        LicenseRepositoryImpl(licenseLocalDataSource, licenseRemoteDataSource, testDispatcherProvider)

    @Before
    fun setup() {
        coEvery { licenseRemoteDataSource.getLicense("invalidProject", any()) } returns null
        coEvery { licenseRemoteDataSource.getLicense("validProject", any()) } returns license
    }

    @Test
    fun `Get license from local`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns license

        val newLicense = licenseRepositoryImpl.getLicense("invalidProject", "deviceId")

        assertThat(newLicense).isEqualTo(license)
    }

    @Test
    fun `Get license from remote if doesn't exist in local, save local`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

        val newLicense = licenseRepositoryImpl.getLicense("validProject", "deviceId")

        assertThat(newLicense).isEqualTo(license)
        verify { licenseLocalDataSource.saveLicense(license) }
    }

    @Test
    fun `Get null license if things go wrong`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns null

        val newLicense = licenseRepositoryImpl.getLicense("invalidProject", "deviceId")

        assertThat(newLicense).isNull()
    }

    @Test
    fun `Get license flow from local`() = runBlockingTest {
        every { licenseLocalDataSource.getLicense() } returns license

        val licenseStates = mutableListOf<LicenseState>()
        licenseRepositoryImpl.getLicenseFlow("invalidProject", "deviceId")
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
        licenseRepositoryImpl.getLicenseFlow("validProject", "deviceId")
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
        licenseRepositoryImpl.getLicenseFlow("invalidProject", "deviceId")
            .toCollection(licenseStates)

        with(licenseStates) {
            assertThat(size).isEqualTo(3)
            assertThat(get(0)).isEqualTo(LicenseState.Started)
            assertThat(get(1)).isEqualTo(LicenseState.Downloading)
            assertThat(get(2)).isEqualTo(LicenseState.FinishedWithError)
        }
    }
}
