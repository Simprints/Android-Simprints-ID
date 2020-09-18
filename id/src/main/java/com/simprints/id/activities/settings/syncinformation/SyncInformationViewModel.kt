package com.simprints.id.activities.settings.syncinformation

import androidx.lifecycle.*
import com.simprints.id.activities.settings.syncinformation.SyncInformationActivity.ViewState.SyncDataFetched
import com.simprints.id.activities.settings.syncinformation.modulecount.ModuleCount
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting.ON
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncState
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerState
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncInformationViewModel(private val personRepository: SubjectRepository,
                               private val subjectLocalDataSource: SubjectLocalDataSource,
                               private val preferencesManager: PreferencesManager,
                               private val projectId: String,
                               private val subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository,
                               private val imageRepository: ImageRepository,
                               private val subjectsSyncManager: SubjectsSyncManager) : ViewModel() {

    fun getViewStateLiveData(): LiveData<SyncInformationActivity.ViewState> = viewStateLiveData

    private val viewStateLiveData = MediatorLiveData<SyncInformationActivity.ViewState>()

    fun updateSyncInfo() {
        viewStateLiveData.addSource(subjectsSyncManager.getLastSyncState().map { it.isRunning() }) {
            viewModelScope.launch {
                if (it) {
                    viewStateLiveData.value = SyncInformationActivity.ViewState.LoadingState.Syncing
                } else {
                    viewStateLiveData.value = SyncInformationActivity.ViewState.LoadingState.Calculating
                    viewStateLiveData.value = fetchRecords()
                }
            }
        }
    }

    internal suspend fun fetchRecords(): SyncDataFetched {
        val recordsInLocalCount = fetchLocalRecordCount()
        val imagesToUploadCount = fetchAndUpdateImagesToUploadCount()
        val recordsToUpSyncCount = fetchAndUpdateRecordsToUpSyncCount()
        val modulesCount = fetchAndUpdateSelectedModulesCount()
        val (recordsToDownSync, recordsToDelete) = fetchRecordsToUpdateAndDeleteCountIfNecessary()
        return SyncDataFetched(
            recordsInLocal = recordsInLocalCount,
            recordsToDownSync = recordsToDownSync,
            recordsToUpSync = recordsToUpSyncCount,
            recordsToDelete = recordsToDelete,
            imagesToUpload = imagesToUploadCount,
            moduleCounts = modulesCount
        )
    }

    internal suspend fun fetchLocalRecordCount() =
        subjectLocalDataSource.count(SubjectLocalDataSource.Query(projectId = projectId))

    private fun fetchAndUpdateImagesToUploadCount() = imageRepository.getNumberOfImagesToUpload()

    internal suspend fun fetchAndUpdateRecordsToUpSyncCount() =
        subjectLocalDataSource.count(SubjectLocalDataSource.Query(toSync = true))

    internal suspend fun fetchRecordsToUpdateAndDeleteCountIfNecessary(): Pair<Int?, Int?> {
        return if(isDownSyncAllowed()) {
            fetchAndUpdateRecordsToDownSyncAndDeleteCount()
        } else {
            Pair(null, null)
        }
    }

    private fun isDownSyncAllowed() = with(preferencesManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    internal suspend fun fetchAndUpdateRecordsToDownSyncAndDeleteCount(): Pair<Int?, Int?> {
        return try {
            val downSyncScope = subjectsDownSyncScopeRepository.getDownSyncScope()
            val counts = personRepository.countToDownSync(downSyncScope)
            Pair(counts.created, counts.deleted)
        } catch (t: Throwable) {
            t.printStackTrace()
            Pair(null, null)
        }
    }

    internal suspend fun fetchAndUpdateSelectedModulesCount() = preferencesManager.selectedModules.map {
            ModuleCount(it,
                subjectLocalDataSource.count(SubjectLocalDataSource.Query(projectId = projectId, moduleId = it)))
        }

    private fun SubjectsSyncState.isRunning(): Boolean {
        val downSyncStates = downSyncWorkersInfo
        val upSyncStates = upSyncWorkersInfo
        val allSyncStates = downSyncStates + upSyncStates
        Timber.d("Sync states -- ${allSyncStates.map { it.state.state }} ")
        return allSyncStates.any {
            it.state is SubjectsSyncWorkerState.Running || it.state is SubjectsSyncWorkerState.Enqueued
        }
    }
}
