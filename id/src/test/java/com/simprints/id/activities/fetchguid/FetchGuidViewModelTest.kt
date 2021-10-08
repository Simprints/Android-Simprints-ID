package com.simprints.id.activities.fetchguid

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.just
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FetchGuidViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: FetchGuidViewModel

    @MockK
    private lateinit var fetchGuidHelper: FetchGuidHelper

    @MockK
    private lateinit var deviceManager: DeviceManager

    @MockK
    private lateinit var eventRepository: com.simprints.eventsystem.event.EventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = FetchGuidViewModel(fetchGuidHelper, deviceManager, eventRepository, timeHelper, testDispatcherProvider)

        configureMocks()
    }


    private fun configureMocks() {
        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
        every { timeHelper.now() } returns CREATED_AT
    }

    @Test
    fun fetchGuidSucceedsFromLocal_shouldReturnCorrectSubjectSource() {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(
                any(),
                any(),
                any()
            )
        } returns SubjectFetchResult(defaultSubject, LOCAL)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(LOCAL)
    }

    @Test
    fun fetchGuidSucceedsFromRemote_shouldReturnCorrectSubjectSource() {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(
                any(),
                any(),
                any()
            )
        } returns SubjectFetchResult(defaultSubject, REMOTE)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(REMOTE)
    }

    @Test
    fun fetchGuidFailsFromLocalAndOffline_shouldReturnFailedOfflineSubjectSource() {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(
                any(),
                any(),
                any()
            )
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { deviceManager.isConnected() } returns false

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
    }

    @Test
    fun fetchGuidFailsFromLocalAndRemoteOnline_shouldReturnNotFoundSubjectSource() {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(
                any(),
                any(),
                any()
            )
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { deviceManager.isConnected() } returns true

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(NOT_FOUND_IN_LOCAL_AND_REMOTE)
    }

    @Test
    fun fetchGuidInLocal_shouldAddCandidateReadEvent() {
        runBlocking {
            coEvery {
                fetchGuidHelper.loadFromRemoteIfNeeded(
                    any(),
                    any(),
                    any()
                )
            } returns SubjectFetchResult(defaultSubject, LOCAL)

            viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

            coVerify {
                eventRepository.addOrUpdateEvent(
                    withArg {
                        assertTrue(
                            it is CandidateReadEvent
                                && it.payload.localResult == LocalResult.FOUND
                                && it.payload.remoteResult == null
                        )
                    }
                )
            }
        }
    }

    @Test
    fun fetchGuidInRemote_shouldAddCandidateReadEvent() {
        runBlocking {
            coEvery {
                fetchGuidHelper.loadFromRemoteIfNeeded(
                    any(),
                    any(),
                    any()
                )
            } returns SubjectFetchResult(defaultSubject, REMOTE)

            viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

            coVerify {
                eventRepository.addOrUpdateEvent(
                    withArg {
                        assertTrue(
                            it is CandidateReadEvent
                                && it.payload.localResult == LocalResult.NOT_FOUND
                                && it.payload.remoteResult == RemoteResult.FOUND
                        )
                    }
                )
            }
        }
    }

    @Test
    fun localGuidNotFoundAndOffline_shouldAddCandidateReadEvent() {
        runBlocking {
            coEvery {
                fetchGuidHelper.loadFromRemoteIfNeeded(
                    any(),
                    any(),
                    any()
                )
            } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
            coEvery { deviceManager.isConnected() } returns false

            viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

            coVerify {
                eventRepository.addOrUpdateEvent(
                    withArg {
                        assertTrue(
                            it is CandidateReadEvent
                                && it.payload.localResult == LocalResult.NOT_FOUND
                                && it.payload.remoteResult == null
                        )
                    }
                )
            }
        }
    }

    @Test
    fun fetchGuidNotFound_shouldAddCandidateReadEvent() {
        runBlocking {
            coEvery {
                fetchGuidHelper.loadFromRemoteIfNeeded(
                    any(),
                    any(),
                    any()
                )
            } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
            coEvery { deviceManager.isConnected() } returns true

            viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

            coVerify {
                eventRepository.addOrUpdateEvent(
                    withArg {
                        assertTrue(
                            it is CandidateReadEvent
                                && it.payload.localResult == LocalResult.NOT_FOUND
                                && it.payload.remoteResult == RemoteResult.NOT_FOUND
                        )
                    }
                )
            }
        }
    }


    companion object {
        private const val PROJECT_ID = "project_id"
        private const val VERIFY_GUID = "verify_guid"
    }
}
