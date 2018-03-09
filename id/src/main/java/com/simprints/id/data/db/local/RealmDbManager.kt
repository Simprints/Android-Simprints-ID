package com.simprints.id.data.db.local

import android.content.Context
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.id.libdata.DataCallback
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Utils.wrapCallback
import com.simprints.libcommon.Person
import io.reactivex.Single
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

    override fun signInToLocal(projectId: String, localDbKey: String): Single<Unit> =
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
                                     group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String,
                                     callback: DataCallback?) {
        val wrappedCallback = wrapCallback("RealmDbManager.loadPeopleFromLocal()", callback)

        val realm = getRealmInstance()
        val request: RealmResults<rl_Person> = when (group) {
            com.simprints.id.libdata.tools.Constants.GROUP.GLOBAL -> realm.where(rl_Person::class.java).findAllAsync()
            com.simprints.id.libdata.tools.Constants.GROUP.USER -> realm.where(rl_Person::class.java).equalTo(USER_ID_FIELD, userId).findAllAsync()
            com.simprints.id.libdata.tools.Constants.GROUP.MODULE -> realm.where(rl_Person::class.java).equalTo(MODULE_ID_FIELD, moduleId).findAllAsync()
        }
        request.addChangeListener({ results: RealmResults<rl_Person> ->
            request.removeAllChangeListeners()
            results.mapTo(destinationList) { it.libPerson }
            realm.close()
            wrappedCallback.onSuccess()
        })
    }

    override fun getPeopleCountFromLocal(group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String): Long {
        val realm = getRealmInstance()
        val count = rl_Person.count(realm, userId, moduleId, group)
        realm.close()
        return count
    }

    override fun getRealmInstance(): Realm {
        realmConfig?.let {
            return Realm.getInstance(it) ?: throw RealmUninitialisedError()
        } ?: throw RealmUninitialisedError()
    }

    override fun getValidRealmConfig(): RealmConfiguration {
        return realmConfig ?: throw RealmUninitialisedError()
    }
}
