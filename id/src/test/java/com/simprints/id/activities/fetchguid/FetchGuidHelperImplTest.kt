package com.simprints.id.activities.fetchguid

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.defaultSubject
import com.simprints.id.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.eventsystem.SubjectFetchResult
import com.simprints.eventsystem.SubjectFetchResult.SubjectSource.*
import com.simprints.eventsystem.subject.SubjectRepository
import com.simprints.eventsystem.subject.local.SubjectQuery
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.EventDownSyncProgress
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class FetchGuidHelperImplTest {

    private lateinit var fetchGuidHelper: FetchGuidHelper

    @MockK lateinit var downSyncHelper: EventDownSyncHelper
    @MockK lateinit var subjectRepository: SubjectRepository
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var crashReportManager: CrashReportManager

    private lateinit var downloadEventsChannel: Channel<EventDownSyncProgress>
    private val op = projectDownSyncScope.operations.first()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        fetchGuidHelper = FetchGuidHelperImpl(downSyncHelper, subjectRepository, preferencesManager, crashReportManager)
        coEvery { preferencesManager.modalities } returns listOf(FINGER)
        runBlocking {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun fetchGuid_shouldFetchLocalDbFirst() {
        runBlockingTest {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } returns flowOf(defaultSubject)

            fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            coVerify { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) }
        }
    }

    @Test
    fun fetchGuid_subjectPresentInLocalDb_shouldReturnIt() {
        runBlockingTest {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } returns flowOf(defaultSubject)

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, LOCAL))
        }
    }

    @Test
    fun fetchGuid_subjectNotPresentInLocalDb_shouldFetchRemotely() {
        runBlocking {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } returns emptyFlow()
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            coVerify {
                downSyncHelper.downSync(any(),
                    com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation(
                        com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery(
                            defaultSubject.projectId,
                            subjectId = defaultSubject.subjectId,
                            modes = DEFAULT_MODES,
                            types = com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.subjectEvents
                        )
                    )
                )
            }
        }
    }

    @Test
    fun fetchGuid_afterFetchingRemotely_shouldTryLoadingTheSubjectFromTheDb() {
        runBlocking {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } returns emptyFlow()
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            coVerify(exactly = 2) { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) }
            assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
        }
    }

    @Test
    fun fetchGuid_afterFetchingRemotely_shouldReturnSubjectIfItWasFetched() {
        runBlocking {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } returnsMany listOf(emptyFlow(), flowOf(defaultSubject))
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            coVerify(exactly = 2) { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) }
            assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, REMOTE))
        }
    }

    @Test
    fun fetchGuid_anythingGoesWrong_shouldReturnNotFound() {
        runBlocking {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } throws Throwable("IO exception")
            val result = fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
        }
    }

    @Test
    fun fetchGuid_downSyncFails_shouldReturnNotFound() {
        runBlocking {
            coEvery { subjectRepository.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1)) } throws Throwable("IO exception")
            val channel = Channel<EventDownSyncProgress>()
            coEvery { downSyncHelper.downSync(any(), any()) } returns channel
            channel.close()

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(this, DEFAULT_PROJECT_ID, GUID1)

            assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EventDownSyncProgress>) {
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { downSyncHelper.downSync(any(), any()) } returns downloadEventsChannel

        progressEvents.forEach {
            downloadEventsChannel.send(it)
        }
        downloadEventsChannel.close()
    }
}
