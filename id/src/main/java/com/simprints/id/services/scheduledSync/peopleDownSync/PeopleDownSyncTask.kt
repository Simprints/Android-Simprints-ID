package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.DownSyncParams
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.sync.room.SyncStatusDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.json.JsonHelper.Companion.gson
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class PeopleDownSyncTask(
    val remoteDbManager: RemoteDbManager,
    val dbManager: DbManager,
    val preferencesManager: PreferencesManager,
    val loginInfoManager: LoginInfoManager,
    val localDbManager: LocalDbManager,
    private val syncStatusDatabaseModel: SyncStatusDao) {

    private val syncApi: PeopleRemoteInterface by lazy {
        dbManager.remote.getPeopleApiClient().blockingGet()
    }

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup,
            preferencesManager.selectedModules, loginInfoManager)
    }

    fun execute() {
        // FIXME : Create multiple DownSyncParams from SyncParams
        val downSyncParam = DownSyncParams(syncParams, localDbManager)
        val responseBody = syncApi.downSync(
            downSyncParam.projectId,
            downSyncParam.userId,
            downSyncParam.moduleId,
            downSyncParam.lastKnownPatientId,
            downSyncParam.lastKnownPatientUpdatedAt)
            .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
            .blockingGet()

        setupReaderAndStartSavingPeopleFromStream(responseBody.byteStream())
            .blockingAwait()
    }

    private fun setupReaderAndStartSavingPeopleFromStream(input: InputStream): Completable =
        Completable.create { result ->
            val reader = JsonReader(InputStreamReader(input) as Reader?)
            try {
                reader.beginArray()
                savePeopleFromStreamAndUpdateDownSyncStatus(reader)
                finishDownload(reader, result)
            } catch (e: Exception) {
                finishDownload(reader, result, e)
                throw e
            }
        }

    private fun savePeopleFromStreamAndUpdateDownSyncStatus(reader: JsonReader) {
        var totalDownloaded = 0
        val peopleToDownSync = syncStatusDatabaseModel.getPeopleToDownSync()
        while (reader.hasNext()) {
            localDbManager.savePeopleFromStreamAndUpdateSyncInfo(reader, gson, syncParams) {
                totalDownloaded++
                syncStatusDatabaseModel.updatePeopleToDownSyncCount(peopleToDownSync - totalDownloaded)
                val shouldDownloadingBatchStop =
                    hasCurrentBatchDownloadedFinished(totalDownloaded, DOWN_BATCH_SIZE_FOR_DOWNLOADING)

                if (shouldDownloadingBatchStop) {
                    updateDownSyncTimestampOnBatchDownload()
                }
                shouldDownloadingBatchStop
            }.subscribeBy(onError = {
                throw it
            })
        }
    }

    private fun hasCurrentBatchDownloadedFinished(totalDownloaded: Int, maxPatientsForBatch: Int) =
        totalDownloaded % maxPatientsForBatch == 0

    private fun updateDownSyncTimestampOnBatchDownload() {
        syncStatusDatabaseModel.updateLastDownSyncTime(System.currentTimeMillis())
    }

    private fun finishDownload(reader: JsonReader,
                               emitter: CompletableEmitter,
                               error: Throwable? = null) {

        Timber.d("Download finished")
        error?.let { Timber.e(error) }
        updateDownSyncTimestampOnBatchDownload()
        reader.endArray()
        reader.close()
        if (error != null) {
            emitter.onError(error)
        } else {
            emitter.onComplete()
        }
    }

    companion object {
        const val DOWN_BATCH_SIZE_FOR_DOWNLOADING = 200
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
