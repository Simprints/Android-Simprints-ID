package com.simprints.id.activities.fetchguid

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.EventDownSyncProgress
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FetchGuidHelperImplTest {

    private lateinit var fetchGuidHelper: FetchGuidHelper

    @MockK
    lateinit var downSyncHelper: EventDownSyncHelper

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var downloadEventsChannel: Channel<EventDownSyncProgress>
    private val op = projectDownSyncScope.operations.first()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        fetchGuidHelper = FetchGuidHelperImpl(
            downSyncHelper,
            enrolmentRecordManager,
            configManager,
            UnconfinedTestDispatcher(),
        )

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
        runTest {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun fetchGuid_shouldFetchLocalDbFirst() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } returns flowOf(defaultSubject)

            fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            coVerify {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            }
        }
    }

    @Test
    fun fetchGuid_subjectPresentInLocalDb_shouldReturnIt() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } returns flowOf(defaultSubject)

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, LOCAL))
        }
    }

    @Test
    fun fetchGuid_subjectNotPresentInLocalDb_shouldFetchRemotely() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } returns emptyFlow()
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            coVerify {
                downSyncHelper.downSync(
                    any(),
                    EventDownSyncOperation(
                        RemoteEventQuery(
                            defaultSubject.projectId,
                            subjectId = GUID1,
                            modes = DEFAULT_MODES,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun fetchGuid_afterFetchingRemotely_shouldTryLoadingTheSubjectFromTheDb() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } returns emptyFlow()
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            coVerify(exactly = 2) {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            }
            assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
        }
    }

    @Test
    fun fetchGuid_afterFetchingRemotely_shouldReturnSubjectIfItWasFetched() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } returnsMany listOf(emptyFlow(), flowOf(defaultSubject))
            mockProgressEmission(listOf(EventDownSyncProgress(op, 0)))

            val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            coVerify(exactly = 2) {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            }
            assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, REMOTE))
        }
    }

    @Test
    fun fetchGuid_anythingGoesWrong_shouldReturnNotFound() {
        runTest {
            coEvery {
                enrolmentRecordManager.load(
                    SubjectQuery(
                        DEFAULT_PROJECT_ID,
                        GUID1
                    )
                )
            } throws Throwable("IO exception")
            val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

            assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
        }
    }

    @Test
    fun fetchGuid_downSyncFails_shouldReturnNotFound() = runTest {
        coEvery {
            enrolmentRecordManager.load(
                SubjectQuery(
                    DEFAULT_PROJECT_ID,
                    GUID1
                )
            )
        } throws Throwable("IO exception")
        val channel = Channel<EventDownSyncProgress>()
        coEvery { downSyncHelper.downSync(any(), any()) } returns channel
        channel.close()

        val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
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
