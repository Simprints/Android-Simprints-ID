package com.simprints.feature.troubleshooting.reordsmigration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.troubleshooting.recordsmigration.RealmToRoomRecordsMigrationViewModel
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RealmToRoomRecordsMigrationViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var flagStore: RealmToRoomMigrationFlagsStore

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var viewModel: RealmToRoomRecordsMigrationViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = RealmToRoomRecordsMigrationViewModel(flagStore, enrolmentRecordRepository)
    }

    @Test
    fun `collectData should post logs with flag store state and local DB info`() = runTest {
        // Given
        val mockFlagState = "flags: ENABLED"
        val mockDbInfo = "Database Version: 1"

        coEvery { flagStore.getStoreStateAsString() } returns mockFlagState
        coEvery { enrolmentRecordRepository.getLocalDBInfo() } returns mockDbInfo

        // When
        viewModel.collectData()

        // Then
        val logs = viewModel.logs.getOrAwaitValue()

        assertThat(logs).hasSize(2)
        assertThat(logs[0].title).isEqualTo("Realm to Room migration flags:")
        assertThat(logs[0].body).isEqualTo(mockFlagState)

        assertThat(logs[1].title).isEqualTo("local db info")
        assertThat(logs[1].body).isEqualTo(mockDbInfo)
    }
}
