package com.simprints.feature.troubleshooting.reordsmigration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.feature.troubleshooting.recordsmigration.RealmToRoomRecordsMigrationViewModel
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

    @MockK
    private lateinit var authStore: AuthStore

    private lateinit var viewModel: RealmToRoomRecordsMigrationViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = RealmToRoomRecordsMigrationViewModel(flagStore, enrolmentRecordRepository, authStore)
    }

    @Test
    fun `collectData should post logs with flag store state and local DB info`() = runTest {
        // Given
        val mockFlagState = "flags: ENABLED"
        val mockDbInfo = "Database Version: 1"

        every { authStore.signedInProjectId } returns "project_id"
        coEvery { flagStore.getStoreStateAsString() } returns mockFlagState
        coEvery { enrolmentRecordRepository.getLocalDBInfo() } returns mockDbInfo

        // When
        viewModel.collectData()

        // Then
        val logs = viewModel.logs.getOrAwaitValue()

        assertThat(logs).hasSize(2)
        assertThat(logs[0].title).isEqualTo("Realm to Room migration flags:")
        assertThat(logs[0].body).isEqualTo(mockFlagState)

        assertThat(logs[1].title).isEqualTo("Local db info")
        assertThat(logs[1].body).isEqualTo(mockDbInfo)
    }

    @Test
    fun `collectData should post no local DB info when user is logged out`() = runTest {
        // Given
        val mockFlagState = "flags: ENABLED"
        val mockDbInfo = "Database Version: 1"

        every { authStore.signedInProjectId } returns ""
        coEvery { flagStore.getStoreStateAsString() } returns mockFlagState
        coEvery { enrolmentRecordRepository.getLocalDBInfo() } returns mockDbInfo

        // When
        viewModel.collectData()

        // Then
        val logs = viewModel.logs.getOrAwaitValue()

        assertThat(logs).hasSize(2)
        assertThat(logs[0].title).isEqualTo("Realm to Room migration flags:")
        assertThat(logs[0].body).isEqualTo(mockFlagState)

        assertThat(logs[1].title).isEqualTo("Local db info")
        assertThat(logs[1].body).isEqualTo("No local db info available for logged out users.")
        coVerify(exactly = 0) { enrolmentRecordRepository.getLocalDBInfo() }
    }
}
