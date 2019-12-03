package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.google.gson.stream.JsonReader
import com.simprints.core.tools.coroutines.retryIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromGetApiToDomain
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncStatus
import com.simprints.id.data.db.syncstatus.downsyncinfo.getStatusId
import com.simprints.id.exceptions.safe.data.db.NoSuchDbSyncInfoException
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.bufferedChunks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.InputStreamReader
import java.io.Reader
import java.util.*

class DownSyncTaskImpl(val personLocalDataSource: PersonLocalDataSource,
                       private val syncInfoLocalDataSource: SyncInfoLocalDataSource,
                       val personRemoteDataSource: PersonRemoteDataSource,
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

    override suspend fun execute(subSyncScope: SubSyncScope) {
        this.subSyncScope = subSyncScope

        try {
            val client = personRemoteDataSource.getPeopleApiClient()
            val response = makeDownSyncApiCallAndGetResponse(client)
            val reader = setupJsonReaderFromResponse(response)
            val flowPeople = createPeopleFlowFromJsonReader(reader)
            flowPeople.bufferedChunks(BATCH_SIZE_FOR_DOWNLOADING).collect {
                saveBatchAndUpdateDownSyncStatus(it)
                decrementAndSavePeopleToDownSyncCount(BATCH_SIZE_FOR_DOWNLOADING)
            }

        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            finishDownload()
        }
    }

    private suspend fun makeDownSyncApiCallAndGetResponse(client: PeopleRemoteInterface): ResponseBody =
        retryIO(times = RETRY_ATTEMPTS_FOR_NETWORK_CALLS) {
            client.downSync(projectId, userId, moduleId, getLastKnownPatientId(), getLastKnownPatientUpdatedAt())
        }

    private fun setupJsonReaderFromResponse(response: ResponseBody): JsonReader =
        JsonReader(InputStreamReader(response.byteStream()) as Reader?)
            .also {
                reader = it
                it.beginArray()
            }


    private fun createPeopleFlowFromJsonReader(reader: JsonReader): Flow<ApiGetPerson> =
        flow {
            while (reader.hasNext()) {
                this.emit(JsonHelper.gson.fromJson(reader, ApiGetPerson::class.java))
            }
        }

    private suspend fun saveBatchAndUpdateDownSyncStatus(batchOfPeople: List<ApiGetPerson>) {
        filterBatchOfPeopleToSyncWithLocal(batchOfPeople)
        Timber.d("Saved batch for ${subSyncScope.uniqueKey}")
        updateLastKnownPatientUpdatedAt(batchOfPeople.last().updatedAt)
        updateLastKnownPatientId(batchOfPeople.last().id)
        updateDownSyncTimestampOnBatchDownload()
    }


    private fun finishDownload() {
        Timber.d("Download finished")
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
            val dbSyncInfo = syncInfoLocalDataSource.load(subSyncScope)
            val currentDownSyncStatus = downSyncDao.getDownSyncStatusForId(getDownSyncId())

            val newDownSyncStatus =
                currentDownSyncStatus?.copy(
                    lastPatientId = dbSyncInfo.lastKnownPatientId,
                    lastPatientUpdatedAt = dbSyncInfo.lastKnownPatientUpdatedAt.time)
                    ?: DownSyncStatus(subSyncScope, dbSyncInfo.lastKnownPatientId, dbSyncInfo.lastKnownPatientUpdatedAt.time)

            downSyncDao.insertOrReplaceDownSyncStatus(newDownSyncStatus)
            syncInfoLocalDataSource.delete(subSyncScope)
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

    private suspend fun filterBatchOfPeopleToSyncWithLocal(batchOfPeople: List<ApiGetPerson>) {
        val batchOfPeopleToSaveInLocal = batchOfPeople.filter { !it.deleted }
        val batchOfPeopleToBeDeleted = batchOfPeople.filter { it.deleted }

        savePeopleBatchInLocal(batchOfPeopleToSaveInLocal)
        deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted)
    }

    private suspend fun savePeopleBatchInLocal(batchOfPeopleToSaveInLocal: List<ApiGetPerson>) {
        if (batchOfPeopleToSaveInLocal.isNotEmpty()) {
            personLocalDataSource.insertOrUpdate(batchOfPeopleToSaveInLocal.map { it.fromGetApiToDomain() })
        }
    }

    private suspend fun deletePeopleBatchFromLocal(batchOfPeopleToBeDeleted: List<ApiGetPerson>) {
        if (batchOfPeopleToBeDeleted.isNotEmpty()) {
            personLocalDataSource.delete(buildQueryForPeopleById(batchOfPeopleToBeDeleted))
        }
    }

    private fun buildQueryForPeopleById(batchOfPeopleToBeDeleted: List<ApiGetPerson>) =
        batchOfPeopleToBeDeleted.map {
            PersonLocalDataSource.Query(personId = it.id)
        }

    private fun updateDownSyncTimestampOnBatchDownload() {
        downSyncDao.updateLastSyncTime(getDownSyncId(), timeHelper.now())
    }

    private fun updateLastKnownPatientUpdatedAt(updatedAt: Date?) {
        downSyncDao.updateLastPatientUpdatedAt(getDownSyncId(), updatedAt?.time ?: 0L)
    }

    private fun updateLastKnownPatientId(patientId: String) {
        downSyncDao.updateLastPatientId(getDownSyncId(), patientId)
    }

    private fun getDownSyncId() = downSyncDao.getStatusId(projectId, userId, moduleId)

    private fun decrementAndSavePeopleToDownSyncCount(decrement: Int) {
        val currentCount = downSyncDao.getDownSyncStatusForId(getDownSyncId())?.totalToDownload
        if (currentCount != null) {
            downSyncDao.updatePeopleToDownSync(getDownSyncId(), currentCount - decrement)
        }
    }

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5
    }
}
