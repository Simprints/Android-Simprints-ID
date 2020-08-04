package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.remote.models.ApiEventCount
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.EnrolmentRecordCreation
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.testtools.UnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

typealias CountInvocation<T, V> = suspend (T) -> V

@RunWith(AndroidJUnit4::class)
class EventRemoteDataSourceImplTest {

    @MockK lateinit var simApiClientFactory: SimApiClientFactory
    @MockK lateinit var simApiClient: SimApiClient<EventRemoteInterface>
    @MockK lateinit var eventRemoteInterface: EventRemoteInterface

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private val query = ApiEventQuery(
        projectId = DEFAULT_PROJECT_ID,
        userId = DEFAULT_USER_ID,
        moduleIds = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
        subjectId = GUID1,
        lastEventId = GUID2,
        modes = listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
        types = listOf(EnrolmentRecordCreation)
    )

    @Before
    @ExperimentalCoroutinesApi
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).setupFirebase()

        coEvery { simApiClient.executeCall<Int>(any(), any()) } coAnswers {
            val args = this.args
            (args[1] as CountInvocation<EventRemoteInterface, Int>).invoke(eventRemoteInterface)
        }

        coEvery { simApiClientFactory.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory)
    }

    @Test
    fun count_shouldMakeANetworkRequest() {
        runBlocking {
            coEvery { eventRemoteInterface.countEvents(any(), any(), any(), any(), any(), any(), any()) } returns listOf(ApiEventCount(EnrolmentRecordCreation, 1))

            val count = eventRemoteDataSource.count(query)

            Truth.assertThat(count).isEqualTo(listOf(EventCount(EventType.ENROLMENT_RECORD_CREATION, 1)))
            coVerify(exactly = 1) {
                eventRemoteInterface.countEvents(
                    DEFAULT_PROJECT_ID,
                    listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                    DEFAULT_USER_ID, GUID1,
                    listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
                    GUID2,
                    listOf(EnrolmentRecordCreation.toString())
                )
            }
        }
    }

    @Test
    fun errorForCountRequestFails_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.countEvents(any(), any(), any(), any(), any(), any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.count(query)
            }
        }
    }

    @Test
    fun getStreaming_shouldMakeANetworkRequest() {
        runBlocking {
            coEvery { eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any(), any()) } returns mockk()

            eventRemoteDataSource.getStreaming(query)

            coVerify(exactly = 1) {
                eventRemoteInterface.downloadEvents(
                    DEFAULT_PROJECT_ID,
                    listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                    DEFAULT_USER_ID, GUID1,
                    listOf(ApiModes.FACE, ApiModes.FINGERPRINT),
                    GUID2,
                    listOf(EnrolmentRecordCreation.toString())
                )
            }
        }
    }

    @Test
    fun getStreamingFails_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.downloadEvents(any(), any(), any(), any(), any(), any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.getStreaming(query)
            }
        }
    }

    @Test
    fun postEvent_shouldUploadEvents() {
        runBlocking {
            coEvery { eventRemoteInterface.uploadEvents(any(), any()) } returns mockk()

            val events = listOf(createSessionCaptureEvent())
            eventRemoteDataSource.post(DEFAULT_PROJECT_ID, events)

            coVerify(exactly = 1) {
                eventRemoteInterface.uploadEvents(
                    DEFAULT_PROJECT_ID,
                    match {
                        assertThat(it.events).containsExactlyElementsIn(events.map { it.fromDomainToApi() })
                        true
                    }
                )
            }
        }
    }

    @Test
    fun postEventFails_shouldThrowAnException() {
        runBlocking {
            coEvery { eventRemoteInterface.uploadEvents(any(), any()) } throws Throwable("Request issue")

            shouldThrow<Throwable> {
                eventRemoteDataSource.post(DEFAULT_PROJECT_ID, listOf(createSessionCaptureEvent()))
            }
        }
    }
}
