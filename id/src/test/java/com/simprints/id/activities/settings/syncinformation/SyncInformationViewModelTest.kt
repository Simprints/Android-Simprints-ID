package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SyncInformationViewModelTest {

    @Mock lateinit var personLocalDataSourceMock: PersonLocalDataSource
    @Mock lateinit var preferencesManagerMock: PreferencesManager
    @Mock lateinit var peopleDownSyncScopeRepositoryMock: PeopleDownSyncScopeRepository
    private lateinit var personRepositoryMock: PersonRepository

    private val projectId = "projectId"
    private lateinit var viewModel: SyncInformationViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        personRepositoryMock = mockk()
        UnitTestConfig(this).rescheduleRxMainThread()
        viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, peopleDownSyncScopeRepositoryMock)
    }

    @Test
    fun fetchCountFromLocal_shouldUpdateValue() {
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
        whenever(peopleDownSyncScopeRepositoryMock) { getDownSyncScope() } thenReturn projectSyncScope
        coEvery { personRepositoryMock.countToDownSync(any()) } returns listOf(peopleCount)

        viewModel.fetchAndUpdateRecordsToDownSyncAndDeleteCount()

        assertThat(viewModel.recordsToDownSyncCount.value).isEqualTo(countInRemoteForCreate)
        assertThat(viewModel.recordsToDeleteCount.value).isEqualTo(countInRemoteForDelete)
    }

    @Test
    fun fetchRecordsToUpSyncCount_shouldUpdateValue() {
        val recordsToUpSyncCount = 123
        mockPersonLocalDataSourceCount(recordsToUpSyncCount)

        viewModel.fetchAndUpdateRecordsToUpSyncCount()

        assertThat(viewModel.recordsToUpSyncCount.value).isEqualTo(recordsToUpSyncCount)
    }

    @Test
    fun fetchSelectedModulesCount_shouldUpdateValue() {
        val moduleName = "module1"
        val countForModule = 123
        whenever(preferencesManagerMock) { selectedModules } thenReturn setOf(moduleName)
        mockPersonLocalDataSourceCount(countForModule)

        viewModel.fetchAndUpdateSelectedModulesCount()

        with(viewModel.selectedModulesCount.value?.first()) {
            assertThat(this?.name).isEqualTo(moduleName)
            assertThat(this?.count).isEqualTo(countForModule)
        }
    }

    @Test
    fun unselectedModulesCountGreaterThanZero_shouldUpdateValue() {
        val selectedModuleName = "module1"
        val unselectedModuleName = "module2"
        val moduleOptions = setOf(selectedModuleName, unselectedModuleName)
        val selectedModuleSet = setOf(selectedModuleName)
        val countForUnselectedModules = 122
        whenever(preferencesManagerMock) { moduleIdOptions } thenReturn moduleOptions
        whenever(preferencesManagerMock) { selectedModules } thenReturn selectedModuleSet
        mockPersonLocalDataSourceCount(countForUnselectedModules)

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        with(viewModel.unselectedModulesCount.value?.first()) {
            assertThat(this?.name).isEqualTo(unselectedModuleName)
            assertThat(this?.count).isEqualTo(countForUnselectedModules)
        }
    }

    @Test
    fun unselectedModulesCountIsZero_shouldUpdateValueAsEmptyList() {
        val selectedModuleName = "module1"
        val unselectedModuleName = "module2"
        val moduleOptions = setOf(selectedModuleName, unselectedModuleName)
        val selectedModuleSet = setOf(selectedModuleName)
        val countForUnselectedModules = 0
        whenever(preferencesManagerMock) { moduleIdOptions } thenReturn moduleOptions
        whenever(preferencesManagerMock) { selectedModules } thenReturn selectedModuleSet
        mockPersonLocalDataSourceCount(countForUnselectedModules)

        viewModel.fetchAndUpdatedUnselectedModulesCount()

        assertThat(viewModel.unselectedModulesCount.value).isEmpty()
    }

    private fun mockPersonLocalDataSourceCount(recordCount: Int) {
        whenever(personLocalDataSourceMock) { count(any()) } thenReturn recordCount
    }
}
