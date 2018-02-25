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
import io.realm.RealmResults
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class RealmDbManager(appContext: Context) : LocalDbManager {

    private var realm: Realm? = null

    // Lifecycle

    init {
        Realm.init(appContext)
    }

    override fun signInToLocal(projectId: String, localDbKey: String): Single<Unit> =
        Single.create<Unit> {
            launch(UI) {
                Timber.d("Signing to Realm project $projectId")
                realm = Realm.getInstance(RealmConfig.get(projectId))
                it.onSuccess(Unit)
            }
        }

    override fun signOutOfLocal() {
        launch(UI) {
            realm?.close() ?: throw RealmUninitialisedError()
        }
    }

    override fun isLocalDbInitialized(projectId: String): Boolean =
        realm != null

    // Data transfer

    override fun savePersonInLocal(fbPerson: fb_Person) {
        realm?.let { realm ->
            rl_Person(fbPerson).save(realm)
        } ?: throw RealmUninitialisedError()
    }

    override fun loadPersonFromLocal(destinationList: MutableList<Person>, guid: String, callback: DataCallback) {
        val wrappedCallback = wrapCallback("DatabaseContext.loadPerson()", callback)
        realm?.let { realm ->
            val rlPerson = realm.where(rl_Person::class.java).equalTo("patientId", guid).findFirst()
            if (rlPerson != null) {
                destinationList.add(rlPerson.libPerson)
                wrappedCallback.onSuccess()
                return
            }
        } ?: throw RealmUninitialisedError()
    }

    override fun loadPeopleFromLocal(destinationList: MutableList<Person>,
                                     group: Constants.GROUP, userId: String, moduleId: String,
                                     callback: DataCallback?) {
        val wrappedCallback = wrapCallback("RealmDbManager.loadPeopleFromLocal()", callback)
        realm?.let { realm ->
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
        } ?: throw RealmUninitialisedError()
    }

    override fun getPeopleCountFromLocal(group: Constants.GROUP, userId: String, moduleId: String): Long {
        realm?.let { realm ->
            return rl_Person.count(realm, userId, moduleId, group)
        } ?: throw RealmUninitialisedError()
    }
}
