package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.DownSyncParams
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.json.JsonHelper.Companion.gson
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.text.DateFormat
import java.util.*

class PeopleDownSyncTask (
    val remoteDbManager: RemoteDbManager,
    val dbManager: DbManager,
    val preferencesManager: PreferencesManager,
    val loginInfoManager: LoginInfoManager,
    val localDbManager: LocalDbManager,
    private val syncStatusDatabase: SyncStatusDatabase) {

    private val syncApi: PeopleRemoteInterface by lazy {
        dbManager.remote.getPeopleApiClient().blockingGet()
    }

    var syncParams by lazyVar {
        SyncTaskParameters.build(preferencesManager.syncGroup,
            preferencesManager.moduleId, loginInfoManager)
    }

    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }

    fun execute() {
        val downSyncParam = DownSyncParams(syncParams, localDbManager)
        syncApi.downSync(
            downSyncParam.projectId,
            downSyncParam.userId,
            downSyncParam.moduleId,
            downSyncParam.lastKnownPatientId,
            downSyncParam.lastKnownPatientUpdatedAt)
            .subscribeBy (
                onSuccess = {
                     savePeopleFromStream(syncParams, it.byteStream())
                         .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
                         .subscribe()
                 },
                onError = {
                    throw it
                }
            )
    }

    private fun savePeopleFromStream(syncParams: SyncTaskParameters,
                                          input: InputStream): Observable<Int> =
        Observable.create<Int> { result ->
            val reader = JsonReader(InputStreamReader(input) as Reader?)
            try {
                reader.beginArray()
                var totalDownloaded = 0
                val peopleToDownSync = syncStatusDatabase.syncStatusModel.getPeopleToDownSync()
                while (reader.hasNext()) {
                    dbManager.local.savePeopleFromStreamAndUpdateSyncInfo(reader, gson, syncParams) {
                        totalDownloaded++
                        syncStatusDatabase.syncStatusModel.updatePeopleToDownSyncCount(peopleToDownSync - totalDownloaded)
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
                finishDownload(reader, result)
            } catch (e: Exception) {
                finishDownload(reader, result, e)
                throw e
            }
        }

    private fun hasCurrentBatchDownloadedFinished(totalDownloaded: Int, maxPatientsForBatch: Int) =
        totalDownloaded % maxPatientsForBatch == 0

    private fun updateDownSyncTimestampOnBatchDownload() {
        syncStatusDatabase.syncStatusModel.updateLastDownSyncTime(dateFormat.format(Date()))
    }

    private fun finishDownload(reader: JsonReader,
                               emitter: Emitter<Int>,
                               error: Throwable? = null) {

        Timber.d("Download finished")
        error?.let { Timber.e(error)}

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
