package com.simprints.infra.sync.devicestate

import com.google.common.truth.Truth.*
import com.simprints.infra.backendapi.ApiResult
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.sync.devicestate.remote.DeviceStatusRemoteInterface
import com.simprints.infra.sync.devicestate.remote.toApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ReportDeviceDataStateUseCaseTest {
    @MockK
    private lateinit var tracker: DeviceStateDataTracker

    @MockK
    private lateinit var backendApiClient: BackendApiClient

    @MockK
    private lateinit var remoteInterface: DeviceStatusRemoteInterface

    private lateinit var useCase: ReportDeviceDataStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { backendApiClient.executeCall<DeviceStatusRemoteInterface, Any>(any(), any()) } coAnswers {
            try {
                ApiResult.Success(secondArg<suspend (DeviceStatusRemoteInterface) -> Any>()(remoteInterface))
            } catch (e: Exception) {
                ApiResult.Failure(e)
            }
        }
        coEvery { remoteInterface.postDeviceStatus(any(), any(), any()) } returns Response.success(null as ResponseBody?)

        useCase = ReportDeviceDataStateUseCase(tracker, backendApiClient, DEVICE_ID)
    }

    @Test
    fun `posts a fresh snapshot for the signed-in project`() = runTest {
        val state = state(projectId = PROJECT_ID)
        coEvery { tracker.getCurrentDeviceDataState() } returns state

        val reported = useCase()

        assertThat(reported).isTrue()
        coVerify { remoteInterface.postDeviceStatus(PROJECT_ID, DEVICE_ID, state.toApi()) }
    }

    @Test
    fun `skips reporting when no project is signed in`() = runTest {
        coEvery { tracker.getCurrentDeviceDataState() } returns state(projectId = null)

        val reported = useCase()

        assertThat(reported).isFalse()
        coVerify(exactly = 0) { backendApiClient.executeCall<DeviceStatusRemoteInterface, Any>(any(), any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `propagates backend exceptions`() = runTest {
        coEvery { tracker.getCurrentDeviceDataState() } returns state(projectId = PROJECT_ID)
        val exception = IllegalStateException("backend down")
        coEvery { remoteInterface.postDeviceStatus(any(), any(), any()) } throws exception

        useCase()
    }

    private fun state(projectId: String?) = DeviceDataState(
        projectId = projectId,
        recordCount = 1,
        pendingScopes = emptyMap(),
        pendingEvents = 2,
        pendingEnrolments = 3,
        pendingSamples = 4,
        lastEventSyncAt = null,
        lastEventSyncFailure = null,
        lastSampleSyncAt = null,
        lastSampleSyncFailure = null,
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
    }
}
