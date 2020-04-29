package com.simprints.id.data.db.person.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.reflect.TypeToken
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.commontesttools.EnrolmentRecordsGeneratorUtils.getRandomEnrolmentEvents
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType.*
import com.simprints.id.data.db.person.domain.personevents.Events
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.json.SimJsonHelper
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
        private const val DEVICE_ID = "device_id"
        private val MODULES = listOf("module1", "module2")
        private const val EVENTS_JSON = "[{\"id\":\"e9257686-663f-4943-943e-09f9fdd9252b\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordCreation\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925sd\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordDeletion\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}},{\"id\":\"e9257686-663f-4943-943e-09f9fdd925dsa\",\"labels\":{\"moduleId\":[\"module1\"],\"projectId\":[\"projectId\"],\"attendantId\":[\"user1\"]},\"payload\":{\"type\":\"EnrolmentRecordMove\",\"subjectId\":\"2d6b446d-e7a6-4665-a2ad-23c4c95f297a\",\"projectId\":\"projectId\",\"attendantId\":\"user1\",\"moduleId\":\"module1\",\"biometricReferences\":[{\"type\":\"FingerprintReference\",\"metadata\":{\"vero\":\"VERO_2\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"finger\":\"LEFT_THUMB\",\"quality\":82}]},{\"type\":\"FaceReference\",\"metadata\":{\"SDK\":\"ML_KIT\"},\"templates\":[{\"template\":\"Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}]}}]"
    }

    private val mockServer = MockWebServer()
    private val mockBaseUrlProvider: BaseUrlProvider = mockk()
    private val gson = SimJsonHelper.gson

    private val eventRemoteDataSourceSpy = spyk(EventRemoteDataSourceImpl(mockk(), mockk()))
    private lateinit var eventRemoteInterface: EventRemoteInterface

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
            val expectedRequestUrlFormat = "/projects/project_id/events"
            eventRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider, DEVICE_ID, gson
            ).build<EventRemoteInterface>().api
            coEvery { eventRemoteDataSourceSpy.getPeopleApiClient() } returns eventRemoteInterface
            mockServer.enqueue(mockSuccessfulResponse())

            eventRemoteDataSourceSpy.post(PROJECT_ID, buildEnrolmentRecordEvents())

            with(mockServer) {
                assertThat(requestCount).isEqualTo(1)
                assertThat(takeRequest().requestUrl.toString()).contains(expectedRequestUrlFormat)
            }
        }
    }

    @Test
    fun successfulResponse_onGetCount_shouldFormCorrectUrlAndEventCounts() {
        runBlocking {
            val expectedCounts = listOf(
                EventCount(EventType.ENROLMENT_RECORD_CREATION, 42),
                EventCount(EventType.ENROLMENT_RECORD_DELETION, 42),
                EventCount(EventType.ENROLMENT_RECORD_MOVE, 42)
            )
            val expectedRequestUrlFormat = "projects/project_id/events/count?l_moduleId=module1&l_moduleId=module2&l_attendantId=user_id&l_subjectId=subject_id&l_mode=FINGERPRINT&l_mode=FACE&lastEventId=last_event_id&type=EnrolmentRecordMove&type=EnrolmentRecordDeletion&type=EnrolmentRecordCreation"
            eventRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider, DEVICE_ID, gson
            ).build<EventRemoteInterface>().api

            coEvery { eventRemoteDataSourceSpy.getPeopleApiClient() } returns eventRemoteInterface
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

    @Test
    fun successfulResponse_get_shouldFormCorrectUrlAndInstances() {
        runBlocking {
            val expectedUrlFormat = "/projects/project_id/events?l_moduleId=module1&l_moduleId=module2&l_attendantId=user_id&l_subjectId=subject_id&l_mode=FINGERPRINT&l_mode=FACE&lastEventId=last_event_id&type=EnrolmentRecordMove&type=EnrolmentRecordDeletion&type=EnrolmentRecordCreation"
            eventRemoteInterface = SimApiClientFactory(
                mockBaseUrlProvider, "deviceId", gson
            ).build<EventRemoteInterface>().api
            coEvery { eventRemoteDataSourceSpy.getPeopleApiClient() } returns eventRemoteInterface
            mockServer.enqueue(buildSuccessfulResponseForGetEvents())

            val responseString = eventRemoteDataSourceSpy.getStreaming(buildEventQuery()).string()

            assertThat(mockServer.requestCount).isEqualTo(1)
            assertThat(mockServer.takeRequest().requestUrl.toString()).contains(expectedUrlFormat)
            val apiEvents = parseApiEventsFromResponse(responseString)
            with (apiEvents) {
                assertThat(size).isEqualTo(3)
                assertThat(get(0).payload).isInstanceOf(ApiEnrolmentRecordCreationPayload::class.java)
                assertThat(get(1).payload).isInstanceOf(ApiEnrolmentRecordDeletionPayload::class.java)
                assertThat(get(2).payload).isInstanceOf(ApiEnrolmentRecordMovePayload::class.java)
            }
        }

    }

    private fun buildEnrolmentRecordEvents() = Events(buildApiEventsList())

    private fun buildApiEventsList() =
        getRandomEnrolmentEvents(5, PROJECT_ID, USER_ID, "module_id")

    private fun buildEventQuery() = EventQuery(
        PROJECT_ID,
        USER_ID,
        MODULES,
        SUBJECT_ID,
        LAST_EVENT_ID,
        listOf(Modes.FINGERPRINT, Modes.FACE),
        listOf(ENROLMENT_RECORD_MOVE,
            ENROLMENT_RECORD_DELETION,
            ENROLMENT_RECORD_CREATION)
    )

    private fun buildSuccessfulResponseForCount() = MockResponse().apply {
        setResponseCode(200)
        setBody("[{\"type\":\"EnrolmentRecordCreation\",\"count\":42},{\"type\":\"EnrolmentRecordDeletion\",\"count\":42},{\"type\":\"EnrolmentRecordMove\",\"count\":42}]")
    }

    private fun buildSuccessfulResponseForGetEvents() = MockResponse().apply {
        setResponseCode(200)
        setBody(EVENTS_JSON)
    }

    private fun parseApiEventsFromResponse(responseString: String): List<ApiEvent> {
        val listType = object : TypeToken<ArrayList<ApiEvent?>?>() {}.type
        return SimJsonHelper.gson.fromJson<List<ApiEvent>>(responseString, listType)
    }
}
