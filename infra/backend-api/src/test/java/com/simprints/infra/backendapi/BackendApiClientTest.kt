package com.simprints.infra.backendapi

import com.google.common.truth.Truth.*
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

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

        assertThat(result).isEqualTo(expectedValue)
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, "token")
        }
    }

    @Test
    fun `executeCall returns Failure when api client returns Failure`() = runTest {
        val throwable = IllegalStateException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        val result = assertThrows<IllegalStateException> {
            subject.executeCall(TestRemoteInterface::class) { "ignored" }
        }

        assertThat(result).isEqualTo(throwable)
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, "token")
        }
    }

    @Test
    fun `executeUnauthenticatedCall returns Success when api client returns Success`() = runTest {
        val expectedValue = "value"
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } returns expectedValue

        val result = subject.executeUnauthenticatedCall(TestRemoteInterface::class) { expectedValue }

        assertThat(result).isEqualTo(expectedValue)
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, null)
        }
    }

    @Test
    fun `executeUnauthenticatedCall returns Failure when api client returns Failure`() = runTest {
        val throwable = IllegalStateException("boom")
        coEvery { apiClient.executeCall(any<suspend (TestRemoteInterface) -> String>()) } throws throwable

        val result = assertThrows<IllegalStateException> {
            subject.executeUnauthenticatedCall(TestRemoteInterface::class) { "ignored" }
        }

        assertThat(result).isEqualTo(throwable)
        coVerify(exactly = 1) {
            simNetwork.getSimApiClient(TestRemoteInterface::class, deviceId, versionName, null)
        }
    }
}
