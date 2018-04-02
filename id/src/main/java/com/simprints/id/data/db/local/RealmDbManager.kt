package com.simprints.id.data.db.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RealmDbManager(private val appContext: Context) : LocalDbManager {


    companion object {
        const val SYNC_ID_FIELD = "syncGroupId"

        const val USER_ID_FIELD = "userId"
        const val PROJECT_ID_FIELD = "projectId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
        const val UPDATE_TIME_FIELD = "updatedAt"

        private const val LEGACY_APP_KEY_LENGTH: Int = 8
    }

    private var realmConfig: RealmConfiguration? = null

    init {
        Realm.init(appContext)
    }

    override fun getRealmInstance(): Realm {
        realmConfig?.let {
            return Realm.getInstance(it) ?: throw RealmUninitialisedError("Error in getInstance")
        } ?: throw RealmUninitialisedError("RealmConfig null")
    }


    override fun signInToLocal(localDbKey: LocalDbKey): Completable = Completable.create { em ->
        Timber.d("Realm sign in. Project: ${localDbKey.projectId} Key: $localDbKey")

        checkLegacyDatabaseAndMigrate(localDbKey)

        realmConfig = RealmConfig.get(localDbKey.projectId, localDbKey.value)
        getRealmInstance().use { em.onComplete() }
    }

    override fun signOutOfLocal() {
        realmConfig = null
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realmConfig != null

    override fun insertOrUpdatePersonInLocal(person: rl_Person): Completable = Completable.create { em ->
        getRealmInstance().use {
            it.executeTransaction { it.insertOrUpdate(person) }
        }.let { em.onComplete() }
    }

    override fun savePersonsFromStreamAndUpdateSyncInfo(readerOfPersonsArray: JsonReader,
                                                        gson: Gson,
                                                        syncParams: SyncTaskParameters,
                                                        shouldStop: (personSaved: fb_Person) -> Boolean) {
        getRealmInstance().use {
            it.executeTransaction {
                while (readerOfPersonsArray.hasNext()) {

                    val lastPersonSaved = parseFromStreamAndSavePerson(gson, readerOfPersonsArray, it)
                    it.insertOrUpdate(RealmSyncInfo(
                        syncGroupId = syncParams.toGroup().ordinal,
                        lastSyncTime = lastPersonSaved.updatedAt ?: Date(0))
                    )

                    if (shouldStop(lastPersonSaved)) {
                        break
                    }

                }
            }
        }
        updateSyncInfo(syncParams)
    }

    override fun getPersonsCountFromLocal(patientId: String?,
                                          projectId: String?,
                                          userId: String?,
                                          moduleId: String?,
                                          toSync: Boolean?): Int {
        return getRealmInstance().use {
            buildQueryForPerson(it, patientId, projectId, userId, moduleId, toSync).count().toInt()
        }
    }

    override fun loadPersonsFromLocal(patientId: String?,
                                      projectId: String?,
                                      userId: String?,
                                      moduleId: String?,
                                      toSync: Boolean?): ArrayList<rl_Person> {
        return getRealmInstance().use {
            val query = buildQueryForPerson(it, patientId, projectId, userId, moduleId, toSync)
            ArrayList(it.copyFromRealm(query.findAll(), 4))
        }
    }

    override fun getValidRealmConfig(): RealmConfiguration {
        return realmConfig ?: throw RealmUninitialisedError()
    }

    override fun getSyncInfoFor(typeSync: Constants.GROUP): RealmSyncInfo? {
        return getRealmInstance().use {
            it.where(RealmSyncInfo::class.java).equalTo(SYNC_ID_FIELD, typeSync.ordinal).findFirst()
        }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    patientId: String? = null,
                                    projectId: String? = null,
                                    userId: String? = null,
                                    moduleId: String? = null,
                                    toSync: Boolean? = null): RealmQuery<rl_Person> {

        return realm.where(rl_Person::class.java).apply {
            projectId?.let { this.equalTo(PROJECT_ID_FIELD, it) }
            patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
            userId?.let { this.equalTo(USER_ID_FIELD, it) }
            moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
            toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
        }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    syncParams: SyncTaskParameters): RealmQuery<rl_Person> {
        return buildQueryForPerson(
            realm = realm,
            projectId = syncParams.projectId,
            userId = syncParams.userId,
            moduleId = syncParams.moduleId
        )
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
        val legacyConfig = getLegacyConfig(dbKey.legacyApiKey, dbKey.legacyRealmKey)

        Realm.getInstance(legacyConfig).use {
            it.writeEncryptedCopyTo(File(appContext.filesDir, "${dbKey.projectId}.realm"), dbKey.value)
        }

        deleteRealm(legacyConfig)
    }

    private fun getLegacyConfig(legacyApiKey: String, legacyDatabaseKey: ByteArray): RealmConfiguration =
        RealmConfig.get(legacyApiKey.substring(0, LEGACY_APP_KEY_LENGTH), legacyDatabaseKey)

    private fun deleteRealm(config: RealmConfiguration) {
        Realm.deleteRealm(config)
        File(appContext.filesDir, "${config.path}.lock").delete()
    }

    private fun updateSyncInfo(syncParams: SyncTaskParameters) {
        getRealmInstance().use { realm ->
            buildQueryForPerson(realm, syncParams)
                .sort(UPDATE_TIME_FIELD, Sort.DESCENDING)
                .findAll()
                .first()?.let { person ->
                    realm.executeTransaction {
                        it.insertOrUpdate(RealmSyncInfo(
                            syncGroupId = syncParams.toGroup().ordinal,
                            lastSyncTime = person.updatedAt ?: Date(0))
                        )
                    }
                }
        }
    }

    private fun parseFromStreamAndSavePerson(gson: Gson,
                                             readerOfPersonsArray: JsonReader,
                                             realm: Realm): fb_Person {
        return gson.fromJson<fb_Person>(readerOfPersonsArray, fb_Person::class.java).apply {
            realm.insertOrUpdate(rl_Person(this))
        }
    }

}
