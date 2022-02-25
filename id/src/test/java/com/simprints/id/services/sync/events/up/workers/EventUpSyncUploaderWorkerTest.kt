package com.simprints.id.services.sync.events.up.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.simprints.core.exceptions.SyncCloudIntegrationException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.services.sync.events.master.internal.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.sync.events.up.EventUpSyncProgress
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.INPUT_UP_SYNC
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventUpSyncUploaderWorkerTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)
    private val scope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
    private lateinit var eventUpSyncUploaderWorker: EventUpSyncUploaderWorker

    @Before
    fun setUp() {
        app.component = mockk(relaxed = true)
        val correctInputData = JsonHelper.toJson(scope)
        eventUpSyncUploaderWorker = createWorker(workDataOf(INPUT_UP_SYNC to correctInputData))
    }

    @Test
    fun worker_shouldExecuteTheTask() {
        runBlocking {
            with(eventUpSyncUploaderWorker) {
                coEvery {
                    upSyncHelper.upSync(any(), any())
                } returns flowOf(
                    EventUpSyncProgress(
                        EventUpSyncOperation(
                            projectId = "",
                            lastState = EventUpSyncOperation.UpSyncState.COMPLETE,
                            lastSyncTime = null
                        ), 12
                    )
                )

                doWork()

                verify { resultSetter.success(workDataOf(EventUpSyncUploaderWorker.OUTPUT_UP_SYNC to 12)) }
            }
        }
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfBackendError() {
        runBlocking {
            val errorResponse =
                "{\"error\":\"002\"}"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<Any>(503, errorResponseBody)
            val exception = HttpException(mockResponse)

            with(eventUpSyncUploaderWorker) {
                coEvery {
                    upSyncHelper.upSync(any(), any())
                } throws exception

                doWork()

                verify {
                    resultSetter.failure(
                        workDataOf(
                            OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                            OUTPUT_ESTIMATED_MAINTENANCE_TIME to null
                        )
                    )
                }
            }
        }
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfTimedBackendError() {
        runBlocking {
            val exception: HttpException = mockk()

            every {
                exception.response()?.errorBody()?.string()
            } returns "{\"error\":\"002\"}"

            every {
                exception.response()?.code()
            } returns 503

            every {
                exception.response()?.headers()
            } returns Headers.Builder()
                .add("Retry-After", "600")
                .build()

            with(eventUpSyncUploaderWorker) {
                coEvery {
                    upSyncHelper.upSync(any(), any())
                } throws exception

                doWork()

                verify {
                    resultSetter.failure(
                        workDataOf(
                            OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                            OUTPUT_ESTIMATED_MAINTENANCE_TIME to 600L
                        )
                    )
                }
            }
        }
    }

    @Test
    fun worker_shouldSetFailCorrectlyIfCloudIntegrationError() {
        runBlocking {
            with(eventUpSyncUploaderWorker) {
                coEvery {
                    upSyncHelper.upSync(any(), any())
                } throws SyncCloudIntegrationException("Cloud integration", Throwable())

                doWork()

                verify {
                    resultSetter.failure(
                        workDataOf(
                            OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true
                        )
                    )
                }
            }
        }
    }

    @Test
    fun eventUpSyncScope_canDeserializeOldFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "queryEvent": {
                    "projectId": "pcmqBbcaB4xWvfRHRELG"
                },
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }    
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

    @Test
    fun eventUpSyncScope_canDeserializeNewFormat() {
        val jsonInput = """
        {
            "type": "EventUpSyncScope${'$'}ProjectScope",
            "projectId": "pcmqBbcaB4xWvfRHRELG",
            "operation": {
                "projectId": "pcmqBbcaB4xWvfRHRELG",
                "lastState": "FAILED",
                "lastSyncTime": 1620103325620
            }
        }
        """.trimIndent()

        val expectedScope = EventUpSyncScope.ProjectScope("pcmqBbcaB4xWvfRHRELG")
        expectedScope.operation.lastState = EventUpSyncOperation.UpSyncState.FAILED
        expectedScope.operation.lastSyncTime = 1620103325620

        val scope = EventUpSyncUploaderWorker.parseUpSyncInput(jsonInput)
        assertThat(scope).isEqualTo(expectedScope)
    }

    private fun createWorker(inputData: Data? = null) =
        (inputData?.let {
            TestListenableWorkerBuilder<EventUpSyncUploaderWorker>(app, inputData = it).build()
        } ?: TestListenableWorkerBuilder<EventUpSyncUploaderWorker>(app).build()).apply {
            resultSetter = mockk(relaxed = true)
            upSyncHelper = mockk(relaxed = true)
            eventSyncCache = mockk(relaxed = true)
            jsonHelper = JsonHelper
            dispatcher = testDispatcherProvider
        }

}
