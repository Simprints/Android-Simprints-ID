package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncSetting
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
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
        viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, peopleDownSyncScopeRepositoryMock)
    }

    @Test
    fun fetchCountFromLocal_shouldUpdateValue() = runBlocking {
        val totalRecordsInLocal = 322
        mockPersonLocalDataSourceCount(totalRecordsInLocal)

        viewModel.fetchAndUpdateLocalRecordCount()

        assertThat(viewModel.localRecordCountLiveData.value).isEqualTo(totalRecordsInLocal)
    }

    @Test
    fun fetchCountFromRemote_shouldUpdateValue() = runBlockingTest {
        val countInRemoteForCreate = 123
        val countInRemoteForMove = 0
        val countInRemoteForDelete = 22
        val eventCounts = listOf(
            EventCount(EventType.EnrolmentRecordCreation, countInRemoteForCreate),
            EventCount(EventType.EnrolmentRecordMove, countInRemoteForMove),
            EventCount(EventType.EnrolmentRecordDeletion, countInRemoteForDelete)
        )
        every { peopleDownSyncScopeRepositoryMock.getDownSyncScope() } returns projectSyncScope
        coEvery { personRepositoryMock.countToDownSync(any()) } returns eventCounts

        viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount()

        assertThat(viewModel.recordsToDownSyncCountLiveData.value).isEqualTo(countInRemoteForCreate)
        assertThat(viewModel.recordsToDeleteCountLiveData.value).isEqualTo(countInRemoteForDelete)
    }

    @Test
    fun fetchRecordsToUpSyncCount_shouldUpdateValue() = runBlockingTest {
        val recordsToUpSyncCount = 123
        mockPersonLocalDataSourceCount(recordsToUpSyncCount)

        viewModel.fetchAndUpdateRecordsToUpSyncCount()

        assertThat(viewModel.recordsToUpSyncCountLiveData.value).isEqualTo(recordsToUpSyncCount)
    }

    @Test
    fun fetchSelectedModulesCount_shouldUpdateValue() = runBlockingTest {
        val moduleName = "module1"
        val countForModule = 123
        every { preferencesManagerMock.selectedModules } returns setOf(moduleName)
        mockPersonLocalDataSourceCount(countForModule)

        viewModel.fetchAndUpdateSelectedModulesCount()

        with(viewModel.selectedModulesCountLiveData.value?.first()) {
            assertThat(this?.name).isEqualTo(moduleName)
            assertThat(this?.count).isEqualTo(countForModule)
        }
    }

    @Test
    fun withUnselectedModules_shouldUpdateValue() = runBlockingTest {
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

        with(viewModel.unselectedModulesCountLiveData.value?.first()) {
            assertThat(this?.name).isEqualTo(unselectedModuleName)
            assertThat(this?.count).isEqualTo(2)
        }
    }

    @Test
    fun withNoUnselectedModules_shouldUpdateValueAsEmptyList() = runBlockingTest {
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

        assertThat(viewModel.unselectedModulesCountLiveData.value).isEmpty()
    }

    @Test
    fun downSyncSettingIsOn_shouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.peopleDownSyncSetting } returns PeopleDownSyncSetting.ON

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 1) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    @Test
    fun downSyncSettingIsExtra_shouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.peopleDownSyncSetting } returns PeopleDownSyncSetting.EXTRA

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 1) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    @Test
    fun downSyncSettingIsOffShouldRequestRecordsToDownloadAndDeleteCount() = runBlockingTest {
        every { preferencesManagerMock.peopleDownSyncSetting } returns PeopleDownSyncSetting.OFF

        viewModel.fetchRecordsToUpdateAndDeleteCountIfNecessary()

        coVerify(exactly = 0) { viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount() }
    }

    private fun mockPersonLocalDataSourceCount(recordCount: Int) {
        coEvery { personLocalDataSourceMock.count(any()) } returns recordCount
    }

}
