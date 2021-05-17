package com.simprints.id.data.license.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.license.repository.LicenseVendor
import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.*

typealias InterfaceInvocation<T, V> = suspend (T) -> V

class LicenseRemoteDataSourceImplTest {
    private val license = UUID.randomUUID().toString()

    private val remoteInterface = mockk<LicenseRemoteInterface>()
    private val simApiClient = mockk<SimApiClient<LicenseRemoteInterface>>()
    private val simApiClientFactory = mockk<SimApiClientFactory>()
    private val licenseRemoteDataSourceImpl = LicenseRemoteDataSourceImpl(simApiClientFactory, JsonHelper)

    @Before
    fun setup() {
        coEvery { simApiClient.executeCall<ApiLicense>(any(), any()) } coAnswers {
            val args = this.args
            (args[1] as InterfaceInvocation<LicenseRemoteInterface, ApiLicense>).invoke(remoteInterface)
        }

        coEvery { simApiClientFactory.buildClient(LicenseRemoteInterface::class) } returns simApiClient

        coEvery { remoteInterface.getLicense("validProject", any(), any()) } returns ApiLicense(
            RankOneLicense(
                "RANK_ONE_FACE",
                "today",
                license
            )
        )
        coEvery { remoteInterface.getLicense("invalidProject", any(), any()) } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(
                    403, "{\"error\":\"001\"}".toResponseBody("application/json".toMediaType())
                )
            )
        )
        coEvery { remoteInterface.getLicense("noQuotaProject", any(), any()) } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(
                    403, "{\"error\":\"002\"}".toResponseBody("application/json".toMediaType())
                )
            )
        )
        coEvery { remoteInterface.getLicense("serviceUnavailable", any(), any()) } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(503, "".toResponseBody("application/json".toMediaType()))
            )
        )
    }

    @Test
    fun `Get license correctly from server`() = runBlockingTest {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense("validProject", "deviceId", LicenseVendor.RANK_ONE_FACE)

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Success(license))
    }

    @Test
    fun `Get no license if is an exception`() = runBlockingTest {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense("invalidProject", "deviceId", LicenseVendor.RANK_ONE_FACE)

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("001"))
    }

    @Test
    fun `Get no license if is an exception - project quota`() = runBlockingTest {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense("noQuotaProject", "deviceId", LicenseVendor.RANK_ONE_FACE)

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("002"))
    }

    @Test
    fun `Get no license if is an exception - generic error`() = runBlockingTest {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense("serviceUnavailable", "deviceId", LicenseVendor.RANK_ONE_FACE)

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("000"))
    }
}
