package com.simprints.id.data.db.remote.people

import com.simprints.id.data.db.remote.people.PeopleApi.Companion.PEOPLE_PATH
import com.simprints.id.data.db.remote.people.PeopleApi.Companion.PROJECT_ID_PATH_PARAM
import com.simprints.id.data.db.remote.people.models.RemoteFingerprint
import com.simprints.id.data.db.remote.people.models.RemotePeopleToPost
import com.simprints.id.data.db.remote.people.models.RemotePerson
import com.simprints.id.network.DefaultOkHttpClientBuilder
import com.simprints.libsimprints.FingerIdentifier
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.gildor.coroutines.retrofit.await

class PeopleApiTest {

    private val server = MockWebServer()
    private val serverBaseUrl by lazy {
        server.url("/").toString()
    }

    private val authToken = "authToken"
    private val authorization = "Bearer: $authToken"

    private val projectId = "projectId"

    private val fingerprint = RemoteFingerprint(
        42,
        "Rk1SACAyMAAAAADSAAABLAGQAMUAxQEAABBfHoCoAE5iAECVAHFnAEBBAIx7AEBLANd7AECyANrRAECRANvUAEBgAOTUAECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA"
    )
    private val fingerprintJson = """{
        "quality": ${fingerprint.quality},
        "template": "${fingerprint.template}"
    }""".withoutWhitespaces()

    private val person = RemotePerson(
        "id",
        "moduleId",
        "userId",
        1528138440123,
        mapOf(FingerIdentifier.LEFT_THUMB.name to listOf(fingerprint))
    )
    private val personJson = """{
        "id": "${person.id}",
        "moduleId": "${person.moduleId}",
        "userId": "${person.userId}",
        "createdAt": ${person.createdAt},
        "fingerprints": {
            "LEFT_THUMB": [$fingerprintJson]
        }
    }""".withoutWhitespaces()

    private val peopleToPost = RemotePeopleToPost(
        listOf(person)
    )
    private val peopleToPostJson = """{
        "patients": [$personJson]
    }""".withoutWhitespaces()

    @Before
    fun setupMockServer() {
        server.start()
    }

    @After
    fun teardownMockServer() {
        server.shutdown()
    }

    @Test
    fun test() = runBlocking {
        val redirectionPath = "/some/path"
        server.enqueueTemporaryRedirectionTo(redirectionPath)
        server.enqueue(MockResponse().setResponseCode(200))

        val okHttpClient = DefaultOkHttpClientBuilder().get().build()
        val peopleApi = PeopleApi.Factory.build(serverBaseUrl, okHttpClient)

        peopleApi
            .uploadPeople(authorization, projectId, peopleToPost)
            .await()

        val expectedPath = PEOPLE_PATH.replace("{$PROJECT_ID_PATH_PARAM}", projectId)

        server
            .takeRequest()
            .assertIsPeoplePost(expectedPath, authorization, peopleToPostJson)

        server
            .takeRequest()
            .assertIsPeoplePost(redirectionPath, authorization, peopleToPostJson)
    }

    private fun MockWebServer.enqueueTemporaryRedirectionTo(path: String) {
        val redirectionLocation = url(path).toString()
        val redirection = MockResponse()
            .setResponseCode(307)
            .setHeader("Location", redirectionLocation)
            enqueue(redirection)
    }

    private fun RecordedRequest.assertIsPeoplePost(
        expectedPath: String,
        expectedAuthorizationHeader: String,
        expectedBody: String
    ) {
        assertEquals(expectedPath, path)
        assertEquals("POST", method)
        assertEquals(expectedAuthorizationHeader, getHeader("Authorization"))
        assertEquals(expectedBody, body.readUtf8())
    }

    private fun String.withoutWhitespaces() =
        replace("\\s".toRegex(), "")

}
