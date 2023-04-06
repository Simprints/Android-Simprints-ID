package com.simprints.id.activities.fetchguid

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.SubjectFetchResult
import com.simprints.id.data.db.SubjectFetchResult.SubjectSource.*
import com.simprints.id.testtools.TestData.defaultSubject
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FetchGuidHelperImplTest {

    private lateinit var fetchGuidHelper: FetchGuidHelper

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var configManager: ConfigManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        fetchGuidHelper = FetchGuidHelperImpl(
            eventSyncManager,
            enrolmentRecordManager,
            configManager,
        )

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(GeneralConfiguration.Modality.FINGERPRINT)
            }
        }
    }

    @Test
    fun fetchGuid_shouldFetchLocalDbFirst() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns flowOf(defaultSubject)

        fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        coVerify {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        }
    }

    @Test
    fun fetchGuid_subjectPresentInLocalDb_shouldReturnIt() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns flowOf(defaultSubject)

        val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, LOCAL))
    }

    @Test
    fun fetchGuid_subjectNotPresentInLocalDb_shouldFetchRemotely() = runTest {
        coEvery { enrolmentRecordManager.load(any()) } returns emptyFlow()

        fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        coVerify {
            eventSyncManager.downSync(DEFAULT_PROJECT_ID, GUID1, any())
        }
    }

    @Test
    fun fetchGuid_afterFetchingRemotely_shouldTryLoadingTheSubjectFromTheDb() = runTest {
        coEvery {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        } returns emptyFlow()

        val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        coVerify(exactly = 2) {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        }
        assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
    }


    @Test
    fun fetchGuid_afterFetchingRemotely_shouldReturnSubjectIfItWasFetched() = runTest {
        coEvery {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        } returnsMany listOf(emptyFlow(), flowOf(defaultSubject))

        val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        coVerify(exactly = 2) {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        }
        assertThat(result).isEqualTo(SubjectFetchResult(defaultSubject, REMOTE))
    }

    @Test
    fun fetchGuid_anythingGoesWrong_shouldReturnNotFound() = runTest {
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

    @Test
    fun fetchGuid_downSyncFails_shouldReturnNotFound() = runTest {
        coEvery {
            enrolmentRecordManager.load(SubjectQuery(DEFAULT_PROJECT_ID, GUID1))
        } throws Throwable("IO exception")
        coEvery { eventSyncManager.downSync(any(), any(), any()) } throws IllegalStateException()

        val result = fetchGuidHelper.loadFromRemoteIfNeeded(DEFAULT_PROJECT_ID, GUID1)

        assertThat(result).isEqualTo(SubjectFetchResult(null, NOT_FOUND_IN_LOCAL_AND_REMOTE))
    }
}
