package com.simprints.infra.backendapi

import com.google.common.truth.Truth.*
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

internal class BackendApiClientTest {
    internal interface TestRemoteInterface : SimRemoteInterface

    @MockK
    lateinit var simNetwork: SimNetwork

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var apiClient: SimNetwork.SimApiClient<TestRemoteInterface>

    private lateinit var subject: BackendApiClient

    private val deviceId = "deviceId"
    private val versionName = "1.2.3"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { authStore.getFirebaseToken() } returns "token"
        coEvery { simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, any()) } returns apiClient

        subject = BackendApiClient(
            simNetwork = simNetwork,
            authStore = authStore,
            deviceId = deviceId,
            versionName = versionName,
        )
    }

    @Test
    fun `executeCall returns Success when api client returns Success`() = runTest {
        val expectedValue = "value"
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } returns expectedValue

        val result = subject.executeCall(TestRemoteInterface::class) { expectedValue }

        assertThat(result).isEqualTo(ApiResult.Success(expectedValue))
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, "token")
        }
    }

    @Test
    fun `executeCall returns Failure when api client returns throws exception`() = runTest {
        val throwable = IOException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        val result = subject.executeCall(TestRemoteInterface::class) { "ignored" }

        assertThat(result).isEqualTo(ApiResult.Failure<String>(throwable))
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, "token")
        }
    }

    @Test(expected = CancellationException::class)
    fun `executeCall rethrows CancellationException`() = runTest {
        val throwable = CancellationException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        subject.executeCall(TestRemoteInterface::class) { "ignored" }
    }

    @Test
    fun `executeUnauthenticatedCall returns Success when api client returns Success`() = runTest {
        val expectedValue = "value"
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } returns expectedValue

        val result = subject.executeUnauthenticatedCall(TestRemoteInterface::class) { expectedValue }

        assertThat(result).isEqualTo(ApiResult.Success(expectedValue))
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, null)
        }
    }

    @Test
    fun `executeUnauthenticatedCall returns Failure when api client throws exeption`() = runTest {
        val throwable = IOException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        val result = subject.executeUnauthenticatedCall(TestRemoteInterface::class) { "ignored" }

        assertThat(result).isEqualTo(ApiResult.Failure<String>(throwable))
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, null)
        }
    }

    @Test(expected = CancellationException::class)
    fun `executeUnauthenticatedCall rethrows CancellationException`() = runTest {
        val throwable = CancellationException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        subject.executeUnauthenticatedCall(TestRemoteInterface::class) { "ignored" }
    }
}
