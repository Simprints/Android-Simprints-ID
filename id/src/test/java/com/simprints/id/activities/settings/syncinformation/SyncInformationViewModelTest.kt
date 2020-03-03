package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationViewModelTest {

    @MockK lateinit var personLocalDataSourceMock: PersonLocalDataSource
    @MockK lateinit var preferencesManagerMock: PreferencesManager
    @MockK lateinit var peopleDownSyncScopeRepositoryMock: PeopleDownSyncScopeRepository
    private lateinit var personRepositoryMock: PersonRepository

    private val projectId = "projectId"
    private lateinit var viewModel: SyncInformationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        personRepositoryMock = mockk()
        UnitTestConfig(this).rescheduleRxMainThread()
        viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, peopleDownSyncScopeRepositoryMock)
    }

    @Test
    fun fetchCountFromLocal_shouldUpdateValue() = runBlocking {
        val totalRecordsInLocal = 322
        mockPersonLocalDataSourceCount(totalRecordsInLocal)

        viewModel.fetchAndUpdateLocalRecordCount()

        assertThat(viewModel.localRecordCount.value).isEqualTo(totalRecordsInLocal)
    }

    @Test
    fun fetchCountFromRemote_shouldUpdateValue() {
        val countInRemoteForCreate = 123
        val countInRemoteForUpdate = 0
        val countInRemoteForDelete = 22
        val peopleCount = PeopleCount(countInRemoteForCreate, countInRemoteForDelete, countInRemoteForUpdate)
        every { peopleDownSyncScopeRepositoryMock.getDownSyncScope() } returns projectSyncScope
        coEvery { personRepositoryMock.countToDownSync(any()) } returns listOf(peopleCount)

        viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount()

        assertThat(viewModel.recordsToDownSyncCount.value).isEqualTo(countInRemoteForCreate)
        assertThat(viewModel.recordsToDeleteCount.value).isEqualTo(countInRemoteForDelete)
    }

    @Test
    fun fetchRecordsToUpSyncCount_shouldUpdateValue() = runBlocking {
        val recordsToUpSyncCount = 123
        mockPersonLocalDataSourceCount(recordsToUpSyncCount)

        viewModel.fetchAndUpdateRecordsToUpSyncCount()

        assertThat(viewModel.recordsToUpSyncCount.value).isEqualTo(recordsToUpSyncCount)
    }

    @Test
    fun fetchSelectedModulesCount_shouldUpdateValue() = runBlocking {
        val moduleName = "module1"
        val countForModule = 123
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        mockPersonLocalDataSourceCount(countForModule)

        viewModel.fetchAndUpdateSelectedModulesCount()

        with(viewModel.selectedModulesCount.value?.first()) {
            assertThat(this?.name).isEqualTo(moduleName)
            assertThat(this?.count).isEqualTo(countForModule)
        }
    }

    @Test
    fun withUnselectedModules_shouldUpdateValue() = runBlocking {
        val selectedModuleName = "module1"
        val unselectedModuleName = "module2"
        val recordWithSelectedModule = Person(
            randomUUID(),
            projectId,
            "some_user_id",
            selectedModuleName
        )

        val recordWithUnselectedModule = recordWithSelectedModule.copy(
            moduleId = unselectedModuleName
        )

        val peopleRecords = flowOf(
            recordWithSelectedModule,
            recordWithUnselectedModule,
            recordWithUnselectedModule
        )
        val selectedModuleSet = setOf(selectedModuleName)

        coEvery { personLocalDataSourceMock.load(any()) } returns peopleRecords
        every { preferencesManagerMock.selectedModules } returns selectedModuleSet

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        with(viewModel.unselectedModulesCount.value?.first()) {
            assertThat(this?.name).isEqualTo(unselectedModuleName)
            assertThat(this?.count).isEqualTo(2)
        }
    }

    @Test
    fun withNoUnselectedModules_shouldUpdateValueAsEmptyList() = runBlocking {
        val selectedModuleName = "module1"
        val recordWithSelectedModule = Person(
            randomUUID(),
            projectId,
            "some_user_id",
            selectedModuleName
        )
        val peopleRecords = flowOf(recordWithSelectedModule, recordWithSelectedModule)
        val selectedModuleSet = setOf(selectedModuleName)

        coEvery { personLocalDataSourceMock.load(any()) } returns peopleRecords
        every { preferencesManagerMock.selectedModules } returns selectedModuleSet

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        assertThat(viewModel.unselectedModulesCount.value).isEmpty()
    }

    private fun mockPersonLocalDataSourceCount(recordCount: Int) {
        coEvery { personLocalDataSourceMock.count(any()) } returns recordCount
    }

}
