package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.DownSyncDao
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.NewSyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.getStatusId
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
import javax.inject.Inject

/**
 * Ridwan - DownSyncTask: task to:
 * a) Make NetworkRequest
 * b) save patients in Realm
 * c) InsertOrUpdate
 *      DownSyncStatus(p,u,m).LastPatientId = X
 *      DownSyncStatus(p,u,m).LastPatientUpdatedAt = X
 *      DownSyncStatus(p,u,m).LastSyncTime = X
 */
class DownSyncTask(component: AppComponent, subSyncScope: SubSyncScope) {

    val projectId = subSyncScope.projectId
    val userId = subSyncScope.userId
    val moduleId = subSyncScope.moduleId

    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase
    private var downSyncDao: DownSyncDao

    private var reader: JsonReader? = null

    init {
        component.inject(this)
        downSyncDao = newSyncStatusDatabase.downSyncStatusModel
    }

    fun execute(): Completable =
        remoteDbManager.getPeopleApiClient()
            .makeDownSyncApiCallAndGetResponse()
            .setupJsonReaderFromResponse()
            .createPeopleObservableFromJsonReader()
            .splitIntoBatches()
            .saveBatchAndUpdateDownSyncStatus()
            .doFinally { finishDownload() }

    private fun Single<out PeopleRemoteInterface>.makeDownSyncApiCallAndGetResponse(): Single<ResponseBody> =
        flatMap {
            it.downSync(
                projectId, userId, moduleId, getLastKnownPatientId(), getLastKnownPatientUpdatedAt()
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
                Timber.d("Saved batch")
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

    private fun getLastKnownPatientId(): String? = downSyncDao.getDownSyncStatusForId(getDownSyncId())?.lastPatientId
    private fun getLastKnownPatientUpdatedAt(): Long? = downSyncDao.getDownSyncStatusForId(getDownSyncId())?.lastPatientUpdatedAt
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
        const val BATCH_SIZE_FOR_DOWNLOADING = 200 // STOPSHIP
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
