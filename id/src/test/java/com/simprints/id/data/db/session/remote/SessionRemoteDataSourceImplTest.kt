package com.simprints.id.data.db.session.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionRemoteDataSourceImplTest {

    private val timeHelper: TimeHelper = TimeHelperImpl()
    private val mockServer = MockWebServer()


    private val sessionRemoteDataSourceSpy = spyk(buildRemoteDataSource())

    private val sessionRemoteInterface =
        SimApiClientFactory("deviceId", endpoint = mockServer.url("/").toString()).build<SessionsRemoteInterface>().api

    @Before
    fun setUp() {
        UnitTestConfig(this).setupFirebase()
        coEvery { sessionRemoteDataSourceSpy.getSessionsApiClient() } returns sessionRemoteInterface
    }

    @Test
    fun closedSessions_shouldBeFilteredOutToBeUploaded() {

        sessionRemoteDataSourceSpy.apply {
            val sessions = listOf(
                createFakeOpenSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            val filterTask = sessions.filterClosedSessions()

            filterTask.apply {
                assertThat(size).isEqualTo(1)
                assertThat(first().isClosed()).isTrue()
            }
        }
    }

    @Test
    fun successfulResponseOnUpload() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(200))

            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            sessionRemoteDataSourceSpy.uploadSessions("projectId", sessions)
            assertThat(mockServer.requestCount).isEqualTo(1)
        }
    }

    @Test
    fun failedResponseThenSuccessfulResponse_shouldTryAgainAndSucceed() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(400))
            mockServer.enqueue(MockResponse().setResponseCode(200))

            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            sessionRemoteDataSourceSpy.uploadSessions("projectId", sessions)
            assertThat(mockServer.requestCount).isEqualTo(2)
        }
    }

    private fun buildRemoteDataSource() =
        SessionRemoteDataSourceImpl(mockk(), mockk())
}
