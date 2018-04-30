package com.simprints.id.data.db.local.realm.models

import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libsimprints.FingerIdentifier
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

open class rl_Person : RealmObject {

    companion object {
        const val USER_ID_FIELD = "userId"
        const val PROJECT_ID_FIELD = "projectId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
        const val UPDATE_TIME_FIELD = "updatedAt"
        const val CREATE_TIME_FIELD = "createdAt"
    }

    @PrimaryKey
    @SerializedName("id")
    lateinit var patientId: String

    @Required
    lateinit var projectId: String

    @Required
    lateinit var userId: String

    @Required
    lateinit var moduleId: String

    var createdAt: Date? = null

    var updatedAt: Date? = null

    var toSync: Boolean = false

    @Required
    lateinit var fingerprints: RealmList<rl_Fingerprint>

    val libPerson: Person
        get() {
            return Person(patientId, ArrayList(fingerprints.mapNotNull {
                try {
                Fingerprint(FingerIdentifier.values()[it.fingerId], it.template!!)
                } catch (arg: IllegalArgumentException) {
                    Timber.tag("FINGERPRINT").d("FAILED")
                    null
                }
            }))
        }

    constructor() {}

    constructor(person: fb_Person) {
        this.patientId = person.patientId
        this.userId = person.userId
        this.createdAt = person.createdAt
        this.updatedAt = person.updatedAt
        this.moduleId = person.moduleId
        this.projectId = person.projectId
        this.fingerprints = RealmList()
        this.toSync = person.updatedAt == null || person.createdAt == null

        fingerprints.addAll(person.fingerprintsAsList
                                  .map { rl_Fingerprint(it) }
                                  .filter { it.template != null })
    }
}
