package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.exceptions.safe.data.db.NoSuchRlSessionInfoException
import com.simprints.id.exceptions.safe.sync.InterruptedSyncException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class DownSyncTaskImpl(val localDbManager: LocalDbManager,
                       val remoteDbManager: RemoteDbManager,
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
            remoteDbManager.getPeopleApiClient()
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
            it.downSync(projectId, userId, moduleId, getLastKnownPatientId(), getLastKnownPatientUpdatedAt()
            ).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS.toLong())
        }

    private fun Single<out ResponseBody>.setupJsonReaderFromResponse(): Single<JsonReader> =
        map { responseBody ->
            JsonReader(InputStreamReader(responseBody.byteStream()) as Reader?)
                .also {
                    reader = it
                    it.beginArray()
                }
        }

    private fun Single<out JsonReader>.createPeopleObservableFromJsonReader(): Observable<fb_Person> =
        flatMapObservable { jsonReader ->
            Observable.create<fb_Person> { emitter ->
                while (jsonReader.hasNext()) {
                    emitter.onNext(JsonHelper.gson.fromJson<fb_Person>(jsonReader, fb_Person::class.java))
                }
                emitter.onComplete()
            }
        }

    private fun Observable<fb_Person>.splitIntoBatches(): Observable<List<fb_Person>> =
        buffer(BATCH_SIZE_FOR_DOWNLOADING)

    private fun Observable<List<fb_Person>>.saveBatchAndUpdateDownSyncStatus(): Completable =
        flatMapCompletable { batchOfPeople ->
            Completable.create { emitter ->
                localDbManager.insertOrUpdatePeopleInLocal(batchOfPeople.map { it.toDomainPerson() }).blockingAwait()
                Timber.d("Saved batch for ${subSyncScope.uniqueKey}")
                decrementAndSavePeopleToDownSyncCount(batchOfPeople.size)
                updateLastKnownPatientUpdatedAt(batchOfPeople.last().updatedAt)
                updateLastKnownPatientId(batchOfPeople.last().patientId)
                updateDownSyncTimestampOnBatchDownload()
                emitter.onComplete()
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

    private fun getLastPatientIdFromRealmAndMigrateIt(): String? = fetchRlSessionInfoFromRealmAndMigrateIt()?.lastPatientId
    private fun getLastPatientUpdatedAtFromRealmAndMigrateIt(): Long? = fetchRlSessionInfoFromRealmAndMigrateIt()?.lastPatientUpdatedAt
    private fun fetchRlSessionInfoFromRealmAndMigrateIt(): DownSyncStatus? {
        return try {
            val rlSyncInfo = localDbManager.getRlSyncInfo(subSyncScope).blockingGet()
            val currentDownSyncStatus = downSyncDao.getDownSyncStatusForId(getDownSyncId())

            val newDownSyncStatus =
                currentDownSyncStatus?.copy(
                    lastPatientId = rlSyncInfo.lastKnownPatientId,
                    lastPatientUpdatedAt = rlSyncInfo.lastKnownPatientUpdatedAt.time)
                    ?: DownSyncStatus(subSyncScope, rlSyncInfo.lastKnownPatientId, rlSyncInfo.lastKnownPatientUpdatedAt.time)

            downSyncDao.insertOrReplaceDownSyncStatus(newDownSyncStatus)
            localDbManager.deleteSyncInfo(subSyncScope)
            newDownSyncStatus
        } catch (t: Throwable) {
            if (t is NoSuchRlSessionInfoException) {
                Timber.e("No such realm session info")
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
