package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.data.db.remote.models.ApiGetPerson
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.exceptions.safe.data.db.NoSuchDbSyncInfoException
import com.simprints.id.exceptions.safe.sync.InterruptedSyncException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.tools.TimeHelper
import com.simprints.core.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class DownSyncTaskImpl(val localDbManager: LocalDbManager,
                       val remotePeopleManager: RemotePeopleManager,
                       val timeHelper: TimeHelper,
                       private val downSyncDao: DownSyncDao) : DownSyncTask {

    lateinit var subSyncScope: SubSyncScope

    val projectId
        get() = subSyncScope.projectId
    val userId
        get() = subSyncScope.userId
    val moduleId
        get() = subSyncScope.moduleId

    private var reader: JsonReader? = null

    override fun execute(subSyncScope: SubSyncScope): Completable {
        this.subSyncScope = subSyncScope
        val syncNeeded = syncNeeded()
        return if (syncNeeded) {
            remotePeopleManager.getPeopleApiClient()
                .makeDownSyncApiCallAndGetResponse()
                .setupJsonReaderFromResponse()
                .createPeopleObservableFromJsonReader()
                .splitIntoBatches()
                .saveBatchAndUpdateDownSyncStatus()
                .doOnError { it.printStackTrace() }
                .doFinally { finishDownload() }
        } else {
            Completable.complete()
        }
    }

    private fun syncNeeded(): Boolean {
        val counter = downSyncDao.getDownSyncStatusForId(getDownSyncId())?.totalToDownload
        counter?.let {
            return when {
                counter > 0 -> {
                    true
                }
                counter == 0 -> {
                    false
                }
                else -> {
                    throw InterruptedSyncException("DownCounter failed for $subSyncScope!")
                }
            }
        } ?: throw InterruptedSyncException("Counter failed for $subSyncScope!")
    }

    private fun Single<out PeopleRemoteInterface>.makeDownSyncApiCallAndGetResponse(): Single<ResponseBody> =
        flatMap {
            it.downSync(projectId, userId, moduleId, getLastKnownPatientId(), getLastKnownPatientUpdatedAt())
                .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
        }

    private fun Single<out ResponseBody>.setupJsonReaderFromResponse(): Single<JsonReader> =
        map { responseBody ->
            JsonReader(InputStreamReader(responseBody.byteStream()) as Reader?)
                .also {
                    reader = it
                    it.beginArray()
                }
        }

    private fun Single<out JsonReader>.createPeopleObservableFromJsonReader(): Observable<ApiGetPerson> =
        flatMapObservable { jsonReader ->
            Observable.create<ApiGetPerson> { emitter ->
                try {
                    while (jsonReader.hasNext()) {
                        emitter.onNext(JsonHelper.gson.fromJson<ApiGetPerson>(jsonReader, ApiGetPerson::class.java))
                    }
                    emitter.onComplete()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    emitter.onError(t)
                }
            }
        }

    private fun Observable<ApiGetPerson>.splitIntoBatches(): Observable<List<ApiGetPerson>> =
        buffer(BATCH_SIZE_FOR_DOWNLOADING)
            .doOnError { it.printStackTrace() }

    private fun Observable<List<ApiGetPerson>>.saveBatchAndUpdateDownSyncStatus(): Completable =
        flatMapCompletable { batchOfPeople ->
            Completable.fromAction {
                localDbManager.insertOrUpdatePeopleInLocal(batchOfPeople.map { it.toDomainPerson() }).blockingAwait()
                Timber.d("Saved batch for ${subSyncScope.uniqueKey}")
                decrementAndSavePeopleToDownSyncCount(batchOfPeople.size)
                updateLastKnownPatientUpdatedAt(batchOfPeople.last().updatedAt)
                updateLastKnownPatientId(batchOfPeople.last().id)
                updateDownSyncTimestampOnBatchDownload()
            }
        }

    private fun finishDownload() {
        Timber.d("Download finished")
        updateDownSyncTimestampOnBatchDownload()
        reader?.endArray()
        reader?.close()
    }

    private fun getLastKnownPatientId(): String? =
        downSyncDao.getDownSyncStatusForId(getDownSyncId())?.lastPatientId
            ?: getLastPatientIdFromRealmAndMigrateIt()

    private fun getLastKnownPatientUpdatedAt(): Long? =
        downSyncDao.getDownSyncStatusForId(getDownSyncId())?.lastPatientUpdatedAt
            ?: getLastPatientUpdatedAtFromRealmAndMigrateIt()

    private fun getLastPatientIdFromRealmAndMigrateIt(): String? = fetchDbSyncInfoFromRealmAndMigrateIt()?.lastPatientId
    private fun getLastPatientUpdatedAtFromRealmAndMigrateIt(): Long? = fetchDbSyncInfoFromRealmAndMigrateIt()?.lastPatientUpdatedAt
    private fun fetchDbSyncInfoFromRealmAndMigrateIt(): DownSyncStatus? {
        return try {
            val dbSyncInfo = localDbManager.getDbSyncInfo(subSyncScope).blockingGet()
            val currentDownSyncStatus = downSyncDao.getDownSyncStatusForId(getDownSyncId())

            val newDownSyncStatus =
                currentDownSyncStatus?.copy(
                    lastPatientId = dbSyncInfo.lastKnownPatientId,
                    lastPatientUpdatedAt = dbSyncInfo.lastKnownPatientUpdatedAt.time)
                    ?: DownSyncStatus(subSyncScope, dbSyncInfo.lastKnownPatientId, dbSyncInfo.lastKnownPatientUpdatedAt.time)

            downSyncDao.insertOrReplaceDownSyncStatus(newDownSyncStatus)
            localDbManager.deleteSyncInfo(subSyncScope)
            newDownSyncStatus
        } catch (t: Throwable) {
            if (t is NoSuchDbSyncInfoException) {
                Timber.e("No such realm sync info")
            } else {
                Timber.e(t)
            }
            null
        }
    }

    private fun updateDownSyncTimestampOnBatchDownload() {
        downSyncDao.updateLastSyncTime(getDownSyncId(), timeHelper.now())
    }

    private fun decrementAndSavePeopleToDownSyncCount(decrement: Int) {
        val currentCount = downSyncDao.getDownSyncStatusForId(getDownSyncId())?.totalToDownload
        if (currentCount != null) {
            downSyncDao.updatePeopleToDownSync(getDownSyncId(), currentCount - decrement)
        }
    }

    private fun updateLastKnownPatientUpdatedAt(updatedAt: Date?) {
        downSyncDao.updateLastPatientUpdatedAt(getDownSyncId(), updatedAt?.time ?: 0L)
    }

    private fun updateLastKnownPatientId(patientId: String) {
        downSyncDao.updateLastPatientId(getDownSyncId(), patientId)
    }

    private fun getDownSyncId() = downSyncDao.getStatusId(projectId, userId, moduleId)

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
