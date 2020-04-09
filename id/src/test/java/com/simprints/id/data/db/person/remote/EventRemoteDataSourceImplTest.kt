package com.simprints.id.data.db.person.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.commontesttools.EnrolmentRecordsGeneratorUtils.getRandomEnrolmentEvents
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperationType
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import com.simprints.id.data.db.person.remote.models.personevents.fromDomainToApi
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.mockserver.mockSuccessfulResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
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

    /*TODO: We will be creating a separate helper for the downsync of the enrolment records.
       Will be adding tests for that in the EnrolmentRecordRepository work*/

    private fun buildEnrolmentRecordEvents() = ApiEvents(buildApiEventsList())

    private fun buildApiEventsList() =
        getRandomEnrolmentEvents(5, "projectId", "userId", "moduleId").map {
            it.fromDomainToApi()
        }

    private fun buildEventQuery() = EventQuery(
        PROJECT_ID,
        USER_ID,
        MODULES,
        SUBJECT_ID,
        LAST_EVENT_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        listOf(EnrolmentRecordOperationType.EnrolmentRecordMove,
            EnrolmentRecordOperationType.EnrolmentRecordDeletion,
            EnrolmentRecordOperationType.EnrolmentRecordCreation)
    )

    private fun buildSuccessfulResponseForCount() = MockResponse().apply {
        setResponseCode(200)
        setBody("[{\"type\":\"EnrolmentRecordCreation\",\"count\":42},{\"type\":\"EnrolmentRecordDeletion\",\"count\":42},{\"type\":\"EnrolmentRecordMove\",\"count\":42}]")
    }
}
