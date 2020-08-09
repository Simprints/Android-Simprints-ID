//package com.simprints.id.activities.fetchguid
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.common.truth.Truth.assertThat
//import com.simprints.id.commontesttools.SubjectsGeneratorUtils
//import com.simprints.id.data.db.SubjectFetchResult
//import com.simprints.id.data.db.event.EventRepository
//import com.simprints.id.data.db.subject.SubjectRepository
//import com.simprints.id.exceptions.unexpected.DownloadingAPersonWhoDoesntExistOnServerException
//import com.simprints.id.testtools.TestApplication
//import com.simprints.id.testtools.UnitTestConfig
//import com.simprints.id.tools.TimeHelper
//import com.simprints.id.tools.device.DeviceManager
//import com.simprints.id.tools.extensions.just
//import com.simprints.testtools.common.livedata.testObserver
//import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
//import io.mockk.MockKAnnotations
//import io.mockk.Runs
//import io.mockk.coEvery
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import okhttp3.ResponseBody.Companion.toResponseBody
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.annotation.Config
//import retrofit2.HttpException
//import retrofit2.Response
//import java.util.*
//
//@RunWith(AndroidJUnit4::class)
//@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
//class FetchGuidViewModelTest {
//
//    @MockK private lateinit var subjectRepository: SubjectRepository
//    @MockK private lateinit var deviceManager: DeviceManager
//    @MockK private lateinit var eventRepository: EventRepository
//    @MockK private lateinit var timeHelper: TimeHelper
//
//    companion object {
//        private const val PROJECT_ID = "project_id"
//        private const val VERIFY_GUID = "verify_guid"
//    }
//
//    @Before
//    fun setUp() {
//        UnitTestConfig(this)
//            .rescheduleRxMainThread()
//            .coroutinesMainThread()
//
//        MockKAnnotations.init(this)
//        configureMocks()
//    }
//
//    private fun configureMocks() {
//        coEvery { eventRepository.addEventToCurrentSession(any()) } just Runs
//        every { timeHelper.now() } returns Date().time
//    }
//
//    @Test
//    fun fetchGuidSucceedsFromLocal_shouldReturnCorrectSubjectSource() {
//        coEvery { subjectRepository.loadFromRemoteIfNeeded(any(), any()) } returns SubjectFetchResult(SubjectsGeneratorUtils.getRandomSubject(), SubjectFetchResult.SubjectSource.LOCAL)
//
//        val viewModel = FetchGuidViewModel(subjectRepository, deviceManager, eventRepository, timeHelper)
//        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)
//
//        val testObserver = viewModel.subjectFetch.testObserver()
//
//        assertThat(testObserver.observedValues).contains(SubjectFetchResult.SubjectSource.LOCAL)
//    }
//
//    @Test
//    fun fetchGuidSucceedsFromRemote_shouldReturnCorrectSubjectSource() {
//        coEvery { subjectRepository.loadFromRemoteIfNeeded(any(), any()) } returns SubjectFetchResult(SubjectsGeneratorUtils.getRandomSubject(), SubjectFetchResult.SubjectSource.REMOTE)
//
//        val viewModel = FetchGuidViewModel(subjectRepository, deviceManager, eventRepository, timeHelper)
//        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)
//
//        val testObserver = viewModel.subjectFetch.testObserver()
//
//        assertThat(testObserver.observedValues).contains(SubjectFetchResult.SubjectSource.REMOTE)
//    }
//
//    @Test
//    fun fetchGuidFailsFromLocalOffline_shouldReturnFailedOfflineSubjectSource() {
//        coEvery { deviceManager.isConnected() } returns false
//        coEvery { subjectRepository.loadFromRemoteIfNeeded(any(), any()) } throws HttpException(Response.error<String>(404, "".toResponseBody(null)))
//
//        val viewModel = FetchGuidViewModel(subjectRepository, deviceManager, eventRepository, timeHelper)
//        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)
//
//        val testObserver = viewModel.subjectFetch.testObserver()
//
//        assertThat(testObserver.observedValues).contains(SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
//    }
//
//    @Test
//    fun fetchGuidFailsFromLocalAndRemoteOnline_shouldReturnNotFoundSubjectSource() {
//        coEvery { deviceManager.isConnected() } returns true
//        coEvery { subjectRepository.loadFromRemoteIfNeeded(any(), any()) } throws DownloadingAPersonWhoDoesntExistOnServerException("")
//
//        val viewModel = FetchGuidViewModel(subjectRepository, deviceManager, eventRepository, timeHelper)
//        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)
//
//        val testObserver = viewModel.subjectFetch.testObserver()
//
//        assertThat(testObserver.observedValues).contains(SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE)
//    }
//}
