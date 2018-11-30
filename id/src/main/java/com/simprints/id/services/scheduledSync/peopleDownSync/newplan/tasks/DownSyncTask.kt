package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks

import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room.NewSyncStatusDatabase
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
class DownSyncTask(component: AppComponent,
                   private val projectId: String,
                   private val userId: String?,
                   private val moduleId: String?) {

    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var newSyncStatusDatabase: NewSyncStatusDatabase

    private var reader: JsonReader? = null

    init {
        component.inject(this)
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

    private fun getLastKnownPatientId(): String = "" //StopShip
    private fun getLastKnownPatientUpdatedAt(): Long = 0
    private fun getPeopleToDownSync(): Int = 0
    private fun updateDownSyncTimestampOnBatchDownload() {
        //syncStatusDatabaseModel.updateLastDownSyncTime(System.currentTimeMillis())
    }
    private fun decrementAndSavePeopleToDownSyncCount(decrement: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun updateLastKnownPatientUpdatedAt(updatedAt: Date?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    private fun updateLastKnownPatientId(patientId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
