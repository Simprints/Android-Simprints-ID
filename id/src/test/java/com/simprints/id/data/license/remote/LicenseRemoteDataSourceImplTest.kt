package com.simprints.id.data.license.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.*

class LicenseRemoteDataSourceImplTest {
    private val mockException: HttpException = mockk(relaxed = true)
    private val license = UUID.randomUUID().toString()
    private val licenseServer = mockk<SimApiClientFactory>()
    private val licenseRemoteDataSourceImpl = LicenseRemoteDataSourceImpl(licenseServer)

    @Before
    fun setup() {
        coEvery { licenseServer.getLicense("validProject", any(), any()) } returns license
        coEvery { licenseServer.getLicense("invalidProject", any(), any()) } throws mockException
    }

    @Test
    fun `Get license correctly from server`() = runBlockingTest {
        val newLicense = licenseRemoteDataSourceImpl.getLicense("validProject", "licenseId")

        assertThat(newLicense).isEqualTo(license)
    }

    @Test
    fun `Get no license if is an exception`() = runBlockingTest {
        val newLicense = licenseRemoteDataSourceImpl.getLicense("invalidProject", "licenseId")

        assertThat(newLicense).isNull()
    }
}
