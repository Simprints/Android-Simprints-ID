package com.simprints.id.data.db.local

import android.content.Context
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.models.realm.RealmConfig
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libdata.tools.Utils.wrapCallback
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmConfiguration
import io.realm.RealmResults
import timber.log.Timber

class RealmDbManager(appContext: Context) : LocalDbManager {

    var realm: Realm? = null
    override var realmConfig: RealmConfiguration? = null

    // Lifecycle

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(projectId: String, localDbKey: String): Single<Unit> =
        Single.create<Unit> {
            Timber.d("Signing to Realm project $projectId with key: $localDbKey")
            realmConfig = RealmConfig.get(localDbKey)
            realm = Realm.getInstance(realmConfig)
            it.onSuccess(Unit)
        }

    override fun signOutOfLocal() {
        realmConfig = null
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realmConfig != null

    // Data transfer

    override fun savePersonInLocal(fbPerson: fb_Person) {
        realm?.let { realm ->
            rl_Person(fbPerson).save(realm)
        } ?: throw RealmUninitialisedError()
    }

    override fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback) {
        val wrappedCallback = wrapCallback("DatabaseContext.loadPerson()", callback)
        val realm = Realm.getInstance(realmConfig)

        val rlPerson = realm.where(rl_Person::class.java).equalTo("patientId", guid).findFirst()
        if (rlPerson != null) {
            destinationList.add(rlPerson.libPerson)
            wrappedCallback.onSuccess()
            return
        }
    }

    override fun loadPeopleFromLocal(destinationList: MutableList<Person>,
                                     group: Constants.GROUP, userId: String, moduleId: String,
                                     callback: DataCallback?) {
        val wrappedCallback = wrapCallback("RealmDbManager.loadPeopleFromLocal()", callback)
        val realm = Realm.getInstance(realmConfig)
        val request: RealmResults<rl_Person> = when (group) {
            Constants.GROUP.GLOBAL -> realm.where(rl_Person::class.java).findAllAsync()
            Constants.GROUP.USER -> realm.where(rl_Person::class.java).equalTo("userId", userId).findAllAsync()
            Constants.GROUP.MODULE -> realm.where(rl_Person::class.java).equalTo("moduleId", moduleId).findAllAsync()
        }
        request.addChangeListener(object : RealmChangeListener<RealmResults<rl_Person>> {
            override fun onChange(results: RealmResults<rl_Person>) {
                request.removeChangeListener(this)
                results.mapTo(destinationList) { it.libPerson }
                realm.close()
                wrappedCallback.onSuccess()
            }
        })
    }

    override fun getPeopleCountFromLocal(group: Constants.GROUP, userId: String, moduleId: String): Long {
        val realm = Realm.getInstance(realmConfig)
        return rl_Person.count(realm, userId, moduleId, group)
    }
}
