package com.simprints.id.activities.fetchguid

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.LOCAL
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_AND_REMOTE
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.REMOTE
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.id.tools.extensions.just
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CandidateReadEvent
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FetchGuidViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: FetchGuidViewModel

    @MockK
    private lateinit var fetchGuidHelper: FetchGuidHelper

    @MockK
    private lateinit var connectivityTracker: ConnectivityTracker

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var exitForHelper: ExitFormHelper

    @MockK
    private lateinit var timeHelper: TimeHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = FetchGuidViewModel(
            fetchGuidHelper,
            connectivityTracker,
            eventRepository,
            timeHelper,
            configManager,
            exitForHelper,
        )

        configureMocks()
    }


    private fun configureMocks() {
        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs
        every { timeHelper.now() } returns CREATED_AT
    }

    @Test
    fun fetchGuidSucceedsFromLocal_shouldReturnCorrectSubjectSource() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(defaultSubject, LOCAL)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(LOCAL)
    }

    @Test
    fun fetchGuidSucceedsFromRemote_shouldReturnCorrectSubjectSource() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(defaultSubject, REMOTE)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(REMOTE)
    }

    @Test
    fun fetchGuidFailsFromLocalAndOffline_shouldReturnFailedOfflineSubjectSource() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { connectivityTracker.isConnected() } returns false

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(NOT_FOUND_IN_LOCAL_REMOTE_CONNECTION_ERROR)
    }

    @Test
    fun fetchGuidFailsFromLocalAndRemoteOnline_shouldReturnNotFoundSubjectSource() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { connectivityTracker.isConnected() } returns true

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        val result = viewModel.subjectFetch.getOrAwaitValue()
        assertThat(result).isEqualTo(NOT_FOUND_IN_LOCAL_AND_REMOTE)
    }

    @Test
    fun fetchGuidInLocal_shouldAddCandidateReadEvent() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(defaultSubject, LOCAL)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        it is CandidateReadEvent
                            && it.payload.localResult == LocalResult.FOUND
                            && it.payload.remoteResult == null
                    ).isTrue()
                }
            )
        }
    }


    @Test
    fun fetchGuidInRemote_shouldAddCandidateReadEvent() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(defaultSubject, REMOTE)

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        it is CandidateReadEvent
                            && it.payload.localResult == LocalResult.NOT_FOUND
                            && it.payload.remoteResult == RemoteResult.FOUND
                    ).isTrue()
                }
            )
        }
    }

    @Test
    fun localGuidNotFoundAndOffline_shouldAddCandidateReadEvent() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { connectivityTracker.isConnected() } returns false

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        it is CandidateReadEvent
                            && it.payload.localResult == LocalResult.NOT_FOUND
                            && it.payload.remoteResult == null
                    ).isTrue()
                }
            )
        }
    }

    @Test
    fun fetchGuidNotFound_shouldAddCandidateReadEvent() = runTest {
        coEvery {
            fetchGuidHelper.loadFromRemoteIfNeeded(any(), any())
        } returns SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE)
        coEvery { connectivityTracker.isConnected() } returns true

        viewModel.fetchGuid(PROJECT_ID, VERIFY_GUID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        it is CandidateReadEvent
                            && it.payload.localResult == LocalResult.NOT_FOUND
                            && it.payload.remoteResult == RemoteResult.NOT_FOUND
                    ).isTrue()
                }
            )
        }
    }

    @Test
    fun startsExitForm_whenCalled() = runTest {
        coEvery { configManager.getProjectConfiguration().general.modalities } returns emptyList()
        every { exitForHelper.getExitFormFromModalities(any()) }.returns(mockk())

        viewModel.startExitForm()

        val result = viewModel.exitForm.getOrAwaitValue()
        assertThat(result.getContentIfNotHandled()).isNotNull()
        assertThat(result.hasBeenHandled).isTrue()
    }


    companion object {
        private const val PROJECT_ID = "project_id"
        private const val VERIFY_GUID = "verify_guid"
    }
}
