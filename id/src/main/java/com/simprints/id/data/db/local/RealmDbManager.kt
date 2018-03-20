package com.simprints.id.data.db.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.tools.Utils.wrapCallback
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.libcommon.Person
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmQuery
import io.realm.RealmResults
import timber.log.Timber

class RealmDbManager(appContext: Context) : LocalDbManager {

    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val PATIENT_ID_FIELD = "patientId"
        private const val MODULE_ID_FIELD = "moduleId"
    }

    private var realmConfig: RealmConfiguration? = null

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(projectId: String, localDbKey: LocalDbKey): Single<Unit> =
        Single.create<Unit> {
            Timber.d("Signing to Realm project $projectId with key: $localDbKey")
            realmConfig = RealmConfig.get(projectId, localDbKey)
            val realm = getRealmInstance()
            realm.close()
            it.onSuccess(Unit)
        }

    override fun signOutOfLocal() {
        realmConfig = null
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realmConfig != null

    // Data transfer
    override fun savePersonInLocal(fbPerson: fb_Person) {
        val realm = getRealmInstance()
        rl_Person(fbPerson).save(realm)
        realm.close()
    }

    override fun savePeopleFromStream(reader: JsonReader, gson: Gson, groupSync: Constants.GROUP, shouldStop: () -> Boolean) {
        val realm = getRealmInstance()
        realm.executeTransaction { r ->
            while (reader.hasNext()) {
                val person = gson.fromJson<fb_Person>(reader, fb_Person::class.java)
                r.insertOrUpdate(rl_Person(person))
                r.insertOrUpdate(RealmSyncInfo(groupSync.ordinal, person.updatedAt))
                if (shouldStop()) {
                    break
                }
            }
        }
        realm.close()
    }

    override fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback) {
        val wrappedCallback = wrapCallback("RealmDbManager.loadPerson()", callback)

        val realm = getRealmInstance()
        val rlPerson = realm.where(rl_Person::class.java).equalTo(PATIENT_ID_FIELD, guid).findFirst()
        if (rlPerson != null) {
            destinationList.add(rlPerson.libPerson)
            wrappedCallback.onSuccess()
        }
        realm.close()
    }

    override fun loadPeopleFromLocal(destinationList: MutableList<Person>,
                                     group: Constants.GROUP, userId: String, moduleId: String,
                                     callback: DataCallback?) {
        val wrappedCallback = wrapCallback("RealmDbManager.loadPeopleFromLocal()", callback)

        val realm = getRealmInstance()
        val request: RealmResults<rl_Person> = when (group) {
            Constants.GROUP.GLOBAL -> realm.where(rl_Person::class.java).findAllAsync()
            Constants.GROUP.USER -> realm.where(rl_Person::class.java).equalTo(USER_ID_FIELD, userId).findAllAsync()
            Constants.GROUP.MODULE -> realm.where(rl_Person::class.java).equalTo(MODULE_ID_FIELD, moduleId).findAllAsync()
        }
        request.addChangeListener({ results: RealmResults<rl_Person> ->
            request.removeAllChangeListeners()
            results.mapTo(destinationList) { it.libPerson }
            realm.close()
            wrappedCallback.onSuccess()
        })
    }

    override fun getPeopleCountFromLocal(personId: String?,
                                         projectId: String?,
                                         userId: String?,
                                         moduleId: String?,
                                         toSync: Boolean?): Int {
        val realm = getRealmInstance()
        val query = buildQueryForPerson(realm, personId, projectId, userId, moduleId, toSync)
        return query.count().toInt().also { realm.close() }
    }

    override fun getPeopleFromLocal(personId: String?,
                                    projectId: String?,
                                    userId: String?,
                                    moduleId: String?,
                                    toSync: Boolean?): ArrayList<rl_Person> {

        val realm = getRealmInstance()
        val query = buildQueryForPerson(realm, personId, projectId, userId, moduleId, toSync)
        return ArrayList(query.findAll()).also { realm.close() }
    }

    private fun buildQueryForPerson(realm: Realm,
                                    personId: String?,
                                    projectId: String?,
                                    userId: String?,
                                    moduleId: String?,
                                    toSync: Boolean?): RealmQuery<rl_Person> {

        val query = realm.where(rl_Person::class.java)
        projectId?.let { query.equalTo("projectId", it) }
        personId?.let { query.equalTo("personId", it) }
        userId?.let { query.equalTo("userId", it) }
        moduleId?.let { query.equalTo("moduleId", it) }
        toSync?.let { query.equalTo("toSync", it) }
        return query
    }

    override fun getRealmInstance(): Realm {
        realmConfig?.let {
            return Realm.getInstance(it) ?: throw RealmUninitialisedError()
        } ?: throw RealmUninitialisedError()
    }

    override fun getValidRealmConfig(): RealmConfiguration {
        return realmConfig ?: throw RealmUninitialisedError()
    }

    override fun getSyncInfoFor(typeSync: Constants.GROUP): RealmSyncInfo? {
        return getRealmInstance().where(RealmSyncInfo::class.java).equalTo("id", typeSync.ordinal).findFirst()
    }
}
