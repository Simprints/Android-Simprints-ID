package com.simprints.infra.license.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.testtools.common.alias.InterfaceInvocation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.UUID

@ExperimentalCoroutinesApi
class LicenseRemoteDataSourceImplTest {
    private val license = UUID.randomUUID().toString()
    private val expirationDate = "2023.12.31"

    private val remoteInterface = mockk<LicenseRemoteInterface>()
    private val simApiClient = mockk<SimNetwork.SimApiClient<LicenseRemoteInterface>>()
    private val authStore = mockk<com.simprints.infra.authstore.AuthStore>()
    private val licenseRemoteDataSourceImpl =
        LicenseRemoteDataSourceImpl(authStore, JsonHelper)

    @Before
    fun setup() {
        coEvery { simApiClient.executeCall<ApiLicense>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<LicenseRemoteInterface, ApiLicense>).invoke(
                remoteInterface,
            )
        }

        coEvery { authStore.buildClient(LicenseRemoteInterface::class) } returns simApiClient

        coEvery {
            remoteInterface.getLicense("validProject", any(), any(), any())
        } returns """{
                "RANK_ONE_FACE": {
                    "vendor": "RANK_ONE_FACE",
                    "expiration": "$expirationDate",
                    "data": "$license",
                    "version": "1.0"
                }
            }
        """

        coEvery {
            remoteInterface.getLicense(
                "invalidProject",
                any(),
                any(),
                any(),
            )
        } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(
                    403,
                    "{\"error\":\"001\"}".toResponseBody("application/json".toMediaType()),
                ),
            ),
        )
        coEvery {
            remoteInterface.getLicense(
                "invalidProjectUnknownErrorCode",
                any(),
                any(),
                any(),
            )
        } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(
                    403,
                    "{\"error\":\"000\"}".toResponseBody("application/json".toMediaType()),
                ),
            ),
        )
        coEvery {
            remoteInterface.getLicense(
                "backendMaintenanceErrorProject",
                any(),
                any(),
                any(),
            )
        } throws BackendMaintenanceException(estimatedOutage = null)

        coEvery {
            remoteInterface.getLicense(
                "noQuotaProject",
                any(),
                any(),
                any(),
            )
        } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(
                    403,
                    "{\"error\":\"002\"}".toResponseBody("application/json".toMediaType()),
                ),
            ),
        )
        coEvery {
            remoteInterface.getLicense(
                "serviceUnavailable",
                any(),
                any(),
                any(),
            )
        } throws SyncCloudIntegrationException(
            cause = HttpException(
                Response.error<ApiLicense>(503, "".toResponseBody("application/json".toMediaType())),
            ),
        )
        coEvery {
            remoteInterface.getLicense(
                "networkConnectionException",
                any(),
                any(),
                any(),
            )
        } throws NetworkConnectionException(
            cause = Throwable(),
        )
        coEvery {
            remoteInterface.getLicense(
                "genericException",
                any(),
                any(),
                any(),
            )
        } throws Exception()
    }

    @Test
    fun `Get license correctly from server`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "validProject",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Success(LicenseValue(expirationDate, license, "1.0")))
    }

    @Test
    fun `Get no license if is an exception`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "invalidProject",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("001"))
    }

    @Test
    fun `Get no license if is an authroization issue`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "invalidProjectUnknownErrorCode",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("000"))
    }

    @Test
    fun `Get no license if is a backend maintenance exception`() = runTest {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "backendMaintenanceErrorProject",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.BackendMaintenanceError())
    }

    @Test
    fun `Get no license if is an exception - project quota`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "noQuotaProject",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("002"))
    }

    @Test
    fun `Get no license if is an exception - generic error`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "serviceUnavailable",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("000"))
    }

    @Test
    fun `Get no license if there is a connection exception - generic error`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "networkConnectionException",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("000"))
    }

    @Test
    fun `Get no license if there is a generic exception - generic error`() = runTest(StandardTestDispatcher()) {
        val newLicense =
            licenseRemoteDataSourceImpl.getLicense(
                "genericException",
                "deviceId",
                RANK_ONE_FACE,
                LicenseVersion.UNLIMITED,
            )

        assertThat(newLicense).isEqualTo(ApiLicenseResult.Error("000"))
    }

    companion object {
        private val RANK_ONE_FACE = Vendor.RankOne
    }
}
