package com.simprints.id.data.db.person.remote.models.personevents

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.commontesttools.EnrolmentRecordsGeneratorUtils.getRandomEnrolmentEvents
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.person.remote.EnrolmentEventRecordRemoteInterface
import com.simprints.id.data.db.person.remote.EventRemoteDataSourceImpl
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.mockserver.mockSuccessfulResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRemoteDataSourceImplTest {

    companion object {
        private const val PROJECT_ID = "project_id"
        private const val USER_ID = "user_id"
        private const val SUBJECT_ID = "subject_id"
        private const val LAST_EVENT_ID = "last_event_id"
        private val MODULES = listOf("module1", "module2")
    }

    private val mockServer = MockWebServer()
    private val mockBaseUrlProvider: BaseUrlProvider = mockk()

    private val eventRemoteDataSourceSpy = spyk(EventRemoteDataSourceImpl(mockk(), mockk()))
    private lateinit var enrolmentEventRecordRemoteInterface: EnrolmentEventRecordRemoteInterface

    @Before
    fun setUp() {
        UnitTestConfig(this).setupFirebase()
        mockServer.start()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns mockServer.url("/").toString()
    }

    @Test
    fun successfulResponse_onWrite() {
        runBlocking {
            enrolmentEventRecordRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider, "deviceId"
            ).build<EnrolmentEventRecordRemoteInterface>().api

            coEvery { eventRemoteDataSourceSpy.getPeopleApiClient() } returns enrolmentEventRecordRemoteInterface
            mockServer.enqueue(mockSuccessfulResponse())

            val events = buildEnrolmentRecordEvents()

            eventRemoteDataSourceSpy.write("projectId", buildEnrolmentRecordEvents())
            assertThat(mockServer.requestCount).isEqualTo(1)
        }
    }

    @Test
    fun successfulResponse_onGetCount_shouldFormCorrectUrlAndEventCounts() {
        runBlocking {
            val expectedCounts = listOf(
                EventCount(EventType.EnrolmentRecordCreation, 42),
                EventCount(EventType.EnrolmentRecordDeletion, 42),
                EventCount(EventType.EnrolmentRecordMove, 42)
            )
            val expectedRequestUrlFormat = "projects/project_id/events/count?l_moduleId=module1&l_moduleId=module2&l_attendantId=user_id&l_subjectId=subject_id&l_mode=FINGERPRINT&l_mode=FACE&lastEventId=last_event_id&type=EnrolmentRecordMove&type=EnrolmentRecordDeletion&type=EnrolmentRecordCreation"

            enrolmentEventRecordRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider, "deviceId"
            ).build<EnrolmentEventRecordRemoteInterface>().api
            coEvery { eventRemoteDataSourceSpy.getPeopleApiClient() } returns enrolmentEventRecordRemoteInterface
            mockServer.enqueue(buildSuccessfulResponseForCount())

            val counts = eventRemoteDataSourceSpy.count(buildEventQuery())

            assertThat(mockServer.requestCount).isEqualTo(1)
            assertThat(mockServer.takeRequest().requestUrl.toString()).contains(expectedRequestUrlFormat)
            counts.forEachIndexed { index, it ->
                assertThat(it.type).isEqualTo(expectedCounts[index].type)
                assertThat(it.count).isEqualTo(expectedCounts[index].count)
            }
        }
    }

    private fun buildEnrolmentRecordEvents() = ApiEvents(buildApiEventsList())

    private fun buildApiEventsList() =
        getRandomEnrolmentEvents(5, "projectId", "userId", "moduleId").map {
            it.fromDomainToApi()
        }

    private fun buildEventQuery() = ApiEventQuery(
        PROJECT_ID,
        USER_ID,
        MODULES,
        SUBJECT_ID,
        LAST_EVENT_ID,
        listOf(ApiModes.FINGERPRINT, ApiModes.FACE),
        listOf(ApiEnrolmentRecordOperationType.EnrolmentRecordMove,
            ApiEnrolmentRecordOperationType.EnrolmentRecordDeletion,
            ApiEnrolmentRecordOperationType.EnrolmentRecordCreation)
    )

    private fun buildSuccessfulResponseForCount() = MockResponse().apply {
        setResponseCode(200)
        setBody("[{\"type\":\"EnrolmentRecordCreation\",\"count\":42},{\"type\":\"EnrolmentRecordDeletion\",\"count\":42},{\"type\":\"EnrolmentRecordMove\",\"count\":42}]")
    }
}
