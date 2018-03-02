package com.simprints.id.sync

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.sync.contracts.DownloadContract
import com.simprints.id.sync.contracts.UploadContract
import com.simprints.id.sync.models.RealmSyncInfo
import io.realm.Realm
import java.lang.Exception
import java.util.*


class Sync(private val collRef: CollectionReference,
           val realm: Realm) {

    /**
     * Upload all pending candidates to Firestore
     * @param contract The UploadContract interface
     * @see UploadContract
     * @param batchSize Optional batch size. Default is 50
     */
    fun upload(contract: UploadContract, batchSize: Int = 50) {
        val uploadCount = realm.where(rl_Person::class.java)
                .equalTo("toSync", true).count().toInt()

        if (uploadCount <= 0) {
            contract.uploadFinished(uploadCount)
            return
        }

        contract.uploadStart(uploadCount)
        uploadIterator(contract, batchSize)
    }

    /**
     * Function that is called recursively to upload all batches of people.
     * When a batch is complete the functions either:
     * - Has another batch to upload, and calls itself again
     * - Has no more batches, and tells the UploadContract that uploading is finished
     */
    private fun uploadIterator(contract: UploadContract, batchSize: Int) {

        val people = realm.where(rl_Person::class.java)
                .equalTo("toSync", true).findAll()

        // Set the end position
        var endPos = batchSize
        if (people.size < batchSize) {
            endPos = people.size
        }

        // Fill the batch
        var count = 0
        val batch = collRef.firestore.batch()

        for (person: rl_Person in people) {
            if (count < endPos) {
                batch.set(collRef.document(person.patientId), fb_Person(person).toMap())
                count++
            }
        }

        // Commit the batch
        batch.commit().addOnSuccessListener {

            // Let realm know people are synced
            realm.executeTransaction {
                count = 0
                for (person: rl_Person in people) {
                    if (count < endPos) {
                        person.toSync = false
                        count++
                    }
                }
            }

            // Check if finished
            if (!people.isEmpty()) {
                contract.uploadUpdate(people.size)
                uploadIterator(contract, batchSize)
            } else {
                contract.uploadFinished(people.size)
            }

        }.addOnFailureListener { exception: Exception ->
            contract.uploadError(exception)
        }
    }

    /**
     * Download all new people from Firestore
     * @param contract The DownloadContract interface
     * @see DownloadContract
     * @param batchSize Optional batch size. Default is 50
     */
    fun download(contract: DownloadContract, batchSize: Long = 50) {
        var lastSync: RealmSyncInfo? = realm.where(RealmSyncInfo::class.java)
                .equalTo("id", 0 as Int).findFirst()

        // Check if this is the first sync
        if (lastSync == null) {
            lastSync = RealmSyncInfo(Date(0))
        }

        val query = collRef.whereGreaterThan("syncTime", lastSync.lastSyncTime).limit(batchSize)

        contract.downloadStart()
        downloadIterator(contract, batchSize, lastSync, query)
    }

    /**
     * Function that is called recursively to download all batches of people.
     * When a batch is complete the functions either:
     * - Has another batch to download, and calls itself again
     * - Has no more batches, and tells the DownloadContract that downloading is finished
     */
    private fun downloadIterator(contract: DownloadContract,
                                 batchSize: Long,
                                 lastSync: RealmSyncInfo,
                                 query: Query) {

        query.get().addOnSuccessListener { snapshot: QuerySnapshot ->

            // Check if it's empty or failed
            if (snapshot.isEmpty) {
                if (snapshot.metadata.isFromCache)
                    contract.downloadFailed()
                else
                    contract.downloadFinished()
                return@addOnSuccessListener
            }

            // Load all the documents into realm + update lastSync
            realm.executeTransaction { realm ->

                for (person: DocumentSnapshot in snapshot) {
                    val fbPerson = person.toObject(fb_Person::class.java)

                    lastSync.lastSyncTime = fbPerson.syncTime

                    realm.copyToRealmOrUpdate(lastSync)
                    realm.copyToRealmOrUpdate(rl_Person(fbPerson))
                }
            }

            contract.downloadUpdate(snapshot.size())

            if (snapshot.size() >= batchSize) {
                val nextQuery = collRef.orderBy("syncTime")
                        .startAfter(snapshot.documents[snapshot.size() - 1])
                        .limit(batchSize)
                downloadIterator(contract, batchSize, lastSync, nextQuery)
            } else {
                contract.downloadFinished()
            }

        }.addOnFailureListener {
            contract.downloadFailed()
        }
    }

    /**
     * Reset the Sync Data
     * @param resetUpload Default true. False = don't reset upload data
     * @param resetDownload Default true. False = don't reset download data
     */
    fun resetSync(resetUpload: Boolean = true, resetDownload: Boolean = true) {
        if (resetUpload) {
            val lastSync = RealmSyncInfo(Date(0))
            realm.executeTransaction { realm.copyToRealmOrUpdate(lastSync) }
        }

        if (resetDownload) {
            val people = realm.where(rl_Person::class.java)
                    .equalTo("toSync", false)
                    .findAll()
            realm.executeTransaction {
                for (person: rl_Person in people) {
                    person.toSync = true
                }
            }

        }
    }
}
