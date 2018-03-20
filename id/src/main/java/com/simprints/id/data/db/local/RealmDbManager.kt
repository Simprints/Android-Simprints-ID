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
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Person
import io.reactivex.Completable
import io.realm.Realm
import io.realm.RealmConfiguration
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

    override fun signInToLocal(projectId: String, localDbKey: LocalDbKey): Completable =
        Completable.create {
            Timber.d("Signing to Realm project $projectId with key: $localDbKey")
            realmConfig = RealmConfig.get(projectId, localDbKey)
            val realm = getRealmInstance()
            realm.close()
            it.onComplete()
        }

    override fun signOutOfLocal() {
        realmConfig = null
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realmConfig != null

    // Data transfer
    override fun savePersonInLocal(fbPerson: fb_Person): Completable {
        val realm = getRealmInstance()
        rl_Person(fbPerson).save(realm)
        realm.close()
        return Completable.complete()
    }

    override fun updatePersonInLocal(fbPerson: fb_Person): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun getPeopleToUpSync(): ArrayList<rl_Person> =
        getRealmInstance().let {
            ArrayList(it.where(rl_Person::class.java).equalTo("toSync", false).findAll())
        }

    override fun getPeopleCountFromLocal(group: Constants.GROUP, userId: String, moduleId: String): Long {
        val realm = getRealmInstance()
        val count = rl_Person.count(realm, userId, moduleId, group)
        realm.close()
        return count
    }

    override fun getPeopleFor(syncParams: SyncTaskParameters): ArrayList<rl_Person> {
        val query = Realm.getInstance(realmConfig).where(rl_Person::class.java)
            .equalTo("toSync", false)
            .equalTo("projectId", syncParams.projectId)
        syncParams.userId?.let { query.equalTo("userId", it) }
        syncParams.moduleId?.let { query.equalTo("moduleId", it) }
        return ArrayList(query.findAll())
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
