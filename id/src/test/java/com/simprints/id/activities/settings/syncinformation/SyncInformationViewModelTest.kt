package com.simprints.id.activities.settings.syncinformation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Single
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
    @Mock lateinit var personRepositoryMock: PersonRepository
    @Mock lateinit var preferencesManagerMock: PreferencesManager
    @Mock lateinit var syncScopesBuilderMock: SyncScopesBuilder
    private val projectId = "projectId"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        UnitTestConfig(this).rescheduleRxMainThread()
    }

    @Test
    fun fetchCountFromLocal_shouldUpdateValue() {
        val totalRecordsInLocal = 322
        whenever(personLocalDataSourceMock) { count(any()) } thenReturn totalRecordsInLocal

        val viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, syncScopesBuilderMock)
        viewModel.fetchAndUpdateLocalRecordCount()

        assertThat(viewModel.localRecordCount.value).isEqualTo(totalRecordsInLocal)
    }

    @Test
    fun fetchCountFromRemote_shouldUpdateValue() {
        val countInRemote = 123
        val peopleCount = PeopleCount(projectId, null, null, null, countInRemote)
        whenever(syncScopesBuilderMock) { buildSyncScope() } thenReturn SyncScope(projectId, null, null)
        whenever(personRepositoryMock) { countToDownSync(any()) } thenReturn Single.just(listOf(peopleCount))

        val viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, syncScopesBuilderMock)
        viewModel.fetchAndUpdateRecordsToDownSyncCount()

        assertThat(viewModel.recordsToDownSyncCount.value).isEqualTo(countInRemote)
    }

    @Test
    fun fetchSelectedModulesCount_shouldUpdateValue() {
        val moduleName = "module1"
        val countForModule = 123
        whenever(preferencesManagerMock) { selectedModules } thenReturn setOf(moduleName)
        whenever(personLocalDataSourceMock) { count(any()) } thenReturn countForModule

        val viewModel = SyncInformationViewModel(personRepositoryMock, personLocalDataSourceMock, preferencesManagerMock, projectId, syncScopesBuilderMock)
        viewModel.fetchAndUpdateSelectedModulesCount()

        assertThat(viewModel.selectedModulesCount.value?.first()?.name).isEqualTo(moduleName)
        assertThat(viewModel.selectedModulesCount.value?.first()?.count).isEqualTo(countForModule)
    }
}
