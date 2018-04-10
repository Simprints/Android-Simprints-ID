package com.simprints.id.data.db.local.realm

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.ProjectIdProvider
import com.simprints.id.data.db.local.LocalDbKeyProvider
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.NotSignedInException
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.Sort
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class RealmDbManagerImpl(private val appContext: Context,
                         private val projectIdProvider: ProjectIdProvider,
                         private val localDbKeyProvider: LocalDbKeyProvider) : LocalDbManager {

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

    private fun getLocalDbKey(): LocalDbKey {
        try {
            val projectId = projectIdProvider.getSignedInProjectId().blockingGet()
            return localDbKeyProvider.getLocalDbKey(projectId).blockingGet()
        } catch (e: Exception) {
            throw NotSignedInException(cause = e)
        }
    }

    private fun getRealmConfig(localDbKey: LocalDbKey) =
        RealmConfig.get(localDbKey.projectId, localDbKey.value)

    private fun getRealmInstance(): Realm {
        return realmConfig.let {
            if (it == null) {
                getRealmConfig(getLocalDbKey()).let {
                    realmConfig = it
                    Realm.getInstance(it)
                }
            } else
                Realm.getInstance(it)
        }
    }

    override fun signInToLocal(): Completable = Completable.create { em ->
        migrateLegacyDatabaseIfRequired(getLocalDbKey())
        getRealmInstance().use { em.onComplete() }
    }

    override fun insertOrUpdatePersonInLocal(person: rl_Person): Completable = Completable.create { em ->
        getRealmInstance().use {
            it.executeTransaction { it.insertOrUpdate(person) }
        }.let { em.onComplete() }
    }

    override fun savePeopleFromStreamAndUpdateSyncInfo(readerOfPeopleArray: JsonReader,
                                                       gson: Gson,
                                                       syncParams: SyncTaskParameters,
                                                       shouldStop: (personSaved: fb_Person) -> Boolean) {
        getRealmInstance().use {
            it.executeTransaction {
                while (readerOfPeopleArray.hasNext()) {

                    val lastPersonSaved = parseFromStreamAndSavePerson(gson, readerOfPeopleArray, it)
                    it.insertOrUpdate(rl_SyncInfo(
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

    override fun getPeopleCountFromLocal(patientId: String?,
                                         projectId: String?,
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Int {
        return getRealmInstance().use {
            buildQueryForPerson(it, patientId, projectId, userId, moduleId, toSync).count().toInt()
        }
    }

    override fun loadPeopleFromLocal(patientId: String?,
                                     projectId: String?,
                                     userId: String?,
                                     moduleId: String?,
                                     toSync: Boolean?): ArrayList<rl_Person> {
        return getRealmInstance().use {
            val query = buildQueryForPerson(it, patientId, projectId, userId, moduleId, toSync)
            ArrayList(it.copyFromRealm(query.findAll(), 4))
        }
    }

    override fun loadPeopleFromLocalRx(patientId: String?,
                                       projectId: String?,
                                       userId: String?,
                                       moduleId: String?,
                                       toSync: Boolean?): Flowable<rl_Person> =
        Flowable.create({ emitter ->
            getRealmInstance().use {
                val query = buildQueryForPerson(it, patientId, projectId, userId, moduleId, toSync)
                val people = query.findAll()
                for (person in people) {
                    emitter.onNext(person)
                }
                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)

    override fun getSyncInfoFor(typeSync: Constants.GROUP): rl_SyncInfo? {
        return getRealmInstance().use {
            it.where(rl_SyncInfo::class.java).equalTo(SYNC_ID_FIELD, typeSync.ordinal).findFirst()
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
                                    syncParams: SyncTaskParameters): RealmQuery<rl_Person> = buildQueryForPerson(
        realm = realm,
        projectId = syncParams.projectId,
        userId = syncParams.userId,
        moduleId = syncParams.moduleId
    )

    private fun migrateLegacyDatabaseIfRequired(dbKey: LocalDbKey) {
        if (checkIfLegacyDatabaseNeedsToMigrate(dbKey))
            migrateLegacyRealm(dbKey)
    }

    private fun checkIfLegacyDatabaseNeedsToMigrate(dbKey: LocalDbKey): Boolean {
        if (dbKey.legacyApiKey.isEmpty())
            return false

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
                        it.insertOrUpdate(rl_SyncInfo(
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
