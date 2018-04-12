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
import com.simprints.libcommon.Person
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
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
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Single<Int> = Single.create { em ->
        getRealmInstance().use {
            em.onSuccess(buildQueryForPerson(it, patientId, userId, moduleId, toSync).count().toInt())
        }
    }

    override fun loadPersonFromLocal(personId: String): Single<Person> = Single.create { em ->
        getRealmInstance().use {
            it.where(rl_Person::class.java).equalTo(PATIENT_ID_FIELD, personId).findFirst().let {
                if (it != null)
                    em.onSuccess(it.libPerson)
                else
                    em.onError(IllegalStateException())
            }
        }
    }

    override fun loadPeopleFromLocal(patientId: String?,
                                     userId: String?,
                                     moduleId: String?,
                                     toSync: Boolean?): Single<ArrayList<rl_Person>> = Single.create { em ->
        getRealmInstance().use {
            val query = buildQueryForPerson(it, patientId, userId, moduleId, toSync)
            em.onSuccess(ArrayList(it.copyFromRealm(query.findAll(), 4)))
        }
    }

    override fun loadPeopleFromLocalRx(patientId: String?,
                                       userId: String?,
                                       moduleId: String?,
                                       toSync: Boolean?): Flowable<rl_Person> =
        Flowable.create({ emitter ->
            getRealmInstance().use {
                val query = buildQueryForPerson(it, patientId, userId, moduleId, toSync)
                val people = query.findAll()
                for (person in people) {
                    emitter.onNext(person)
                }
                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)

    override fun getSyncInfoFor(typeSync: Constants.GROUP): Single<rl_SyncInfo> = Single.create { em ->
        getRealmInstance().use {
            it.where(rl_SyncInfo::class.java)
                .equalTo(SYNC_ID_FIELD, typeSync.ordinal)
                .findFirst().let {
                    if (it == null)
                        em.onError(IllegalStateException())
                    else
                        em.onSuccess(it)
                }
        }
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
            if (it == null)
                getRealmConfig(getLocalDbKey()).let { realmConfig = it; Realm.getInstance(it) }
            else
                Realm.getInstance(it)
        }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    patientId: String? = null,
                                    userId: String? = null,
                                    moduleId: String? = null,
                                    toSync: Boolean? = null): RealmQuery<rl_Person> {

        return realm.where(rl_Person::class.java).apply {
            patientId?.let { this.equalTo(PATIENT_ID_FIELD, it) }
            userId?.let { this.equalTo(USER_ID_FIELD, it) }
            moduleId?.let { this.equalTo(MODULE_ID_FIELD, it) }
            toSync?.let { this.equalTo(TO_SYNC_FIELD, it) }
        }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    syncParams: SyncTaskParameters): RealmQuery<rl_Person> = buildQueryForPerson(
        realm = realm,
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
