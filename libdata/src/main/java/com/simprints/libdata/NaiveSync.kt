package com.simprints.libdata

import com.google.firebase.database.*
import com.simprints.libcommon.Progress
import com.simprints.libdata.exceptions.safe.InterruptedSyncException
import com.simprints.libdata.exceptions.unsafe.UnexpectedSyncError
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.models.firebase.fb_User
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Routes.patientNode
import com.simprints.libdata.tools.Routes.userPatientListNode
import io.reactivex.Emitter
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.doAsync
import timber.log.Timber
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

class NaiveSync(
        private val isInterrupted: () -> Boolean,
        private val emitter: Emitter<Progress>,
        private val userId: String = "",
        private val realmConfig: RealmConfiguration,
        private val projectRef: DatabaseReference,
        private val usersRef: DatabaseReference,
        private val patientsRef: DatabaseReference,
        private val batchSize: Int = 50) {

    private var patientsToDownload: ArrayList<String> = ArrayList()
    private var patientsToSave: ArrayList<rl_Person> = ArrayList()
    private var downloadingCount = 0
    private lateinit var realm: Realm

    private val currentProgress = AtomicInteger(0)
    private val maxProgress = AtomicInteger(0)

    /**
     * This method starts the sync by downloading all of the required users and calling
     * setPatientList()
     */
    fun sync() {
        // All firebase callbacks are executed on the ui thread, so we initialize the realm on
        // this thread and make sure that it will only be used on this thread.
        async(UI) {
            realm = Realm.getInstance(realmConfig)

            // Get the query
            val query: Query = if (userId.isBlank()) {
                usersRef
            } else {
                usersRef.orderByChild(userId).limitToFirst(1)
            }

            // Set the user return listener
            query.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.childrenCount <= 0) {
                        emitter.onComplete()
                        return
                    }

                    setDownloadListAndUpload(dataSnapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    emitter.onError(UnexpectedSyncError(error.toException()))
                }
            })
        }
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

            maxProgress.set(patientsToDownload.size)
            emitProgress()

            clearPatientList()
        }

    }

    /**
     * This function calls itself recursively until all patients are downloaded
     */
    private fun clearPatientList() {

        if (isInterrupted()) {
            emitter.onError(InterruptedSyncException())
            return
        }

        if (patientsToDownload.isEmpty() && downloadingCount <= 0) {
            if (!patientsToSave.isEmpty())
                saveBatch()
            emitter.onComplete()
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
                            Timber.d("LOAD FAILED")
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

        currentProgress.set(maxProgress.get() - patientsToDownload.size)
        emitProgress()

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

    private fun emitProgress() {
        emitter.onNext(Progress(currentProgress.get(), maxProgress.get()))
    }

}
