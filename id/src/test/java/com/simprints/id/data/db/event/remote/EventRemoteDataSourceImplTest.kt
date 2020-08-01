package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventRemoteDataSourceImplTest {

    private val timeHelper: TimeHelper = TimeHelperImpl()

    @MockK
    lateinit var simApiClientFactory: SimApiClientFactory
    @MockK
    lateinit var simApiClient: SimApiClient<EventRemoteInterface>
    lateinit var eventRemoteDataSource: EventRemoteDataSource

    @Before
    @ExperimentalCoroutinesApi
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        UnitTestConfig(this).setupFirebase()

        coEvery { simApiClientFactory.buildClient(EventRemoteInterface::class) } returns simApiClient
        eventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory)
    }

//    @Test
//    fun successfulResponseOnUpload() {
//        runBlocking {
//
//            val sessions = listOf(
//                createFakeClosedSession(timeHelper),
//                createFakeClosedSession(timeHelper)
//            )
//
//            eventRemoteDataSource.uploadSessions("projectId", sessions)
//
//            coVerify(exactly = 1) { simApiClient.executeCall(any(), any()) }
//        }
//    }
}
