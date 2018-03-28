package com.simprints.id.data.db.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.models.rl_Fingerprint
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.realm.*
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RealmDbManager(private val appContext: Context) : LocalDbManager {

    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val PROJECT_ID_FIELD = "projectId"
        private const val PATIENT_ID_FIELD = "patientId"
        private const val MODULE_ID_FIELD = "moduleId"
        private const val TO_SYNC_FIELD = "toSync"
        private const val UPDATED_FIELD = "updatedAt"

        private const val LEGACY_APP_KEY_LENGTH: Int = 8
    }

    private var realmConfig: RealmConfiguration? = null

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(localDbKey: LocalDbKey): Completable = Completable.create {
        Timber.d("Realm sign in. Project: ${localDbKey.projectId} Key: $localDbKey")

        //TODO DELETE THIS LINE
        addFakeLegacyData(localDbKey)
        checkLegacyDatabaseAndMigrate(localDbKey)

        realmConfig = RealmConfig.get(localDbKey.projectId, localDbKey.value)
        val realm = getRealmInstance()
        realm.close()
        it.onComplete()
    }

    override fun signOutOfLocal() {
        realmConfig = null
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realmConfig != null

    override fun insertOrUpdatePersonInLocal(person: rl_Person): Completable {
        val realm = getRealmInstance()
        realm.executeTransaction {
            it.insertOrUpdate(person)
        }
        realm.close()
        return Completable.complete()
    }

    override fun savePeopleFromStream(reader: JsonReader, gson: Gson, groupSync: Constants.GROUP, shouldStop: () -> Boolean) {
        val realm = getRealmInstance()
        realm.executeTransaction { r ->
            while (reader.hasNext()) {
                val person = gson.fromJson<fb_Person>(reader, fb_Person::class.java)
                r.insertOrUpdate(rl_Person(person))

                val lastUpdatedTime = person.updatedAt
                if (lastUpdatedTime != null) {
                    r.insertOrUpdate(RealmSyncInfo(groupSync.ordinal, lastUpdatedTime))
                }
                if (shouldStop()) {
                    break
                }
            }
        }
        realm.close()
    }

    override fun updateSyncInfo(syncParams: SyncTaskParameters) {
        val realm = getRealmInstance()
        realm.executeTransaction { r ->
            val query = buildQueryForPerson(r,
                projectId = syncParams.projectId,
                moduleId = syncParams.moduleId,
                userId = syncParams.userId,
                toSync = false)

            val lastTimestamp = query.sort(UPDATED_FIELD, Sort.DESCENDING).findFirst()
            r.insertOrUpdate(RealmSyncInfo(syncParams.toGroup().ordinal, lastTimestamp?.updatedAt
                ?: Date(0)))
        }
        realm.close()
    }

    override fun getPersonsCountFromLocal(patientId: String?,
                                          projectId: String?,
                                          userId: String?,
                                          moduleId: String?,
                                          toSync: Boolean?): Int {
        val realm = getRealmInstance()
        val query = buildQueryForPerson(realm, patientId, projectId, userId, moduleId, toSync)
        return query.count().toInt().also { realm.close() }
    }

    override fun loadPersonsFromLocal(patientId: String?,
                                      projectId: String?,
                                      userId: String?,
                                      moduleId: String?,
                                      toSync: Boolean?): ArrayList<rl_Person> {

        val realm = getRealmInstance()
        val query = buildQueryForPerson(realm, patientId, projectId, userId, moduleId, toSync)
        return ArrayList(realm.copyFromRealm(query.findAll(), 4)).also { realm.close() }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    patientId: String? = null,
                                    projectId: String? = null,
                                    userId: String? = null,
                                    moduleId: String? = null,
                                    toSync: Boolean? = null): RealmQuery<rl_Person> {

        val query = realm.where(rl_Person::class.java)
        projectId?.let { query.equalTo(PROJECT_ID_FIELD, it) }
        patientId?.let { query.equalTo(PATIENT_ID_FIELD, it) }
        userId?.let { query.equalTo(USER_ID_FIELD, it) }
        moduleId?.let { query.equalTo(MODULE_ID_FIELD, it) }
        toSync?.let { query.equalTo(TO_SYNC_FIELD, it) }
        return query
    }

    override fun getRealmInstance(): Realm {
        realmConfig?.let {
            return Realm.getInstance(it) ?: throw RealmUninitialisedError("Error in getInstance")
        } ?: throw RealmUninitialisedError("RealmConfig null")
    }

    override fun getValidRealmConfig(): RealmConfiguration {
        return realmConfig ?: throw RealmUninitialisedError()
    }

    override fun getSyncInfoFor(typeSync: Constants.GROUP): RealmSyncInfo? {
        return getRealmInstance().where(RealmSyncInfo::class.java).equalTo("id", typeSync.ordinal).findFirst()
    }

    private fun checkLegacyDatabaseAndMigrate(dbKey: LocalDbKey) {
        if (dbKey.legacyApiKey.isEmpty())
            return

        if (needsToMigrate(dbKey))
            migrateLegacyRealm(dbKey)
    }

    private fun needsToMigrate(dbKey: LocalDbKey): Boolean {
        val legacyConfig = getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey)
        val newConfig = RealmConfig.get(dbKey.projectId, dbKey.value)

        return File(legacyConfig.path).exists() && !File(newConfig.path).exists()
    }

    private fun migrateLegacyRealm(dbKey: LocalDbKey) {
        Realm.getInstance(getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey)).apply {
            writeEncryptedCopyTo(File(appContext.filesDir, "${dbKey.projectId}.realm"), dbKey.value)
            close()
        }
        Realm.deleteRealm(getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey))
    }

    private fun getLegacyConfig(legacyApiKey: String, legacyDatabaseKey: ByteArray): RealmConfiguration =
        RealmConfig.get(legacyApiKey.substring(0, LEGACY_APP_KEY_LENGTH), legacyDatabaseKey)

    //TODO Delete this temp method
    private fun addFakeLegacyData(dbKey: LocalDbKey) {
        val person = rl_Person().apply {
            patientId = "123"
            userId = "123"
            moduleId = "123"
            projectId = "123"
            fingerprints = RealmList<rl_Fingerprint>()
        }
        val realm = Realm.getInstance(getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey))
        realm.executeTransaction {
            it.copyToRealmOrUpdate(person)
        }
        realm.close()
    }

}
