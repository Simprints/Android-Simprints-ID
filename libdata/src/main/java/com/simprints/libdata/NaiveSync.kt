package com.simprints.libdata

import android.util.Log
import com.google.firebase.database.*
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.models.firebase.fb_User
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Routes.patientNode
import com.simprints.libdata.tools.Routes.userPatientListNode
import io.realm.Realm
import io.realm.RealmConfiguration
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.HashMap
import kotlin.collections.ArrayList

class NaiveSync(
        private val userId: String = "",
        private val callback: SyncCallback,
        private val realmConfig: RealmConfiguration,
        private val projectRef: DatabaseReference,
        private val usersRef: DatabaseReference,
        private val patientsRef: DatabaseReference,
        private val batchSize: Int = 50) {

    private var patientsToDownload: ArrayList<String> = ArrayList()
    private var patientsToSave: ArrayList<rl_Person> = ArrayList()
    private var downloadingCount = 0
    private val realm = Realm.getInstance(realmConfig)

    /**
     * This method starts the sync by downloading all of the required users and calling
     * setPatientList()
     */
    fun sync() {

        // Get the query
        val query: Query = if (userId.isBlank()) {
            usersRef
        } else {
            usersRef.orderByChild(userId).limitToFirst(1)
        }

        callback.onInit()

        // Set the user return listener
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.childrenCount <= 0) {
                    callback.onFinish()
                    return
                }

                setDownloadListAndUpload(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(DATA_ERROR.SYNC_INTERRUPTED)
            }

        })

    }

    /**
     * This functions adds patients to download to the list and uploads patients that need to be
     * saved to remote. When the download list is complete it calls clearPatientList()
     */
    private fun setDownloadListAndUpload(dataSnapshot: DataSnapshot) {

        // TODO Take out using threads. From now on threading will be handled by the caller, not the callee.
        doAsync {
            val bgRealm = Realm.getInstance(realmConfig)

            dataSnapshot.children.forEach {
                val user = it.getValue(fb_User::class.java) ?: return@forEach

                val remoteList = ArrayList(user.patientList.keys)
                remoteList.sort()

                val localList: List<String> = bgRealm.where(rl_Person::class.java)
                        .equalTo("userId", user.userId)
                        .findAllSorted("patientId")
                        .mapNotNull { rl_Person ->
                            rl_Person.patientId
                        }

                patientsToDownload.addAll(remoteList.minus(localList))
                localList.minus(remoteList).forEach { realmToFirebase(bgRealm, it, user) }
            }

            bgRealm.close()

            uiThread {
                callback.onStart(patientsToDownload.size)
                clearPatientList()
            }
        }

    }

    /**
     * This function calls itself recursively until all patients are downloaded
     */
    private fun clearPatientList() {

        if (patientsToDownload.isEmpty() && downloadingCount <= 0) {
            if (!patientsToSave.isEmpty())
                saveBatch()
            callback.onFinish()
            return
        }

        while (downloadingCount < batchSize) {

            if (patientsToDownload.isEmpty())
                return

            downloadingCount++

            val iterator = patientsToDownload.iterator()
            val nextPatientId = iterator.next()
            iterator.remove()

            patientsRef.child(nextPatientId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            downloadingCount--

                            val person = dataSnapshot.getValue(fb_Person::class.java) ?: return
                            patientsToSave.add(rl_Person(person))
                            clearPatientList()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            downloadingCount--
                            clearPatientList()
                            Log.d("LOAD FAILED", "LOAD FAILED")
                        }

                    })
        }

        // Note, this is checked after every call to clearPatientList()
        // irrespective to the addListenerForSingleValueEvent() callbacks
        if (patientsToSave.size >= batchSize) {
            saveBatch()
        }

    }

    private fun saveBatch() {
        val batch: ArrayList<rl_Person> = ArrayList()

        val iterator = patientsToSave.iterator()
        while (iterator.hasNext()) {
            batch.add(iterator.next())
            iterator.remove()
        }

        callback.onProgress(patientsToDownload.size)

        realm.executeTransactionAsync { realm ->
            realm.copyToRealmOrUpdate(batch)
        }
    }

    private fun realmToFirebase(realm: Realm, personId: String, user: fb_User) {
        val person = realm.where(rl_Person::class.java)
                .equalTo("patientId", personId)
                .findFirst() ?: return

        val updates = HashMap<String, Any>()
        updates.put(patientNode(person.patientId), fb_Person(person).toMap())
        updates.put(userPatientListNode(user.userId, person.patientId), true)
        projectRef.updateChildren(updates)
    }

}

