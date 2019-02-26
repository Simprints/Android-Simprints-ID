package com.simprints.id.data.db.local.realm.models

import com.simprints.id.FingerIdentifier
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.Person
import com.simprints.id.tools.extensions.toRealmList
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.simprints.id.domain.fingerprint.Fingerprint as LibFingerprint
import com.simprints.id.domain.fingerprint.Person as LibPerson

open class rl_Person(
    @PrimaryKey
    var patientId: String = "",

    @Required
    var projectId: String = "",

    @Required
    var userId: String = "",

    @Required
    var moduleId: String = "",

    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    var toSync: Boolean = false,

    @Required
    var fingerprints: RealmList<rl_Fingerprint> = RealmList()
) : RealmObject() {

    companion object {
        const val USER_ID_FIELD = "userId"
        const val PROJECT_ID_FIELD = "projectId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
        const val UPDATE_TIME_FIELD = "updatedAt"
        const val CREATE_TIME_FIELD = "createdAt"
    }


    val libPerson: LibPerson
        get() {
            return LibPerson(patientId, ArrayList(fingerprints.mapNotNull {
                try {
                LibFingerprint(FingerIdentifier.values()[it.fingerId], it.template!!)
                } catch (arg: IllegalArgumentException) {
                    Timber.tag("FINGERPRINT").d("FAILED")
                    null
                }
            }))
        }

    constructor(person: fb_Person, toSync: Boolean = person.updatedAt == null || person.createdAt == null):
     this(person.patientId, person.projectId, person.userId, person.moduleId, person.createdAt, person.updatedAt, toSync,
         person.fingerprintsAsList
             .map { it.toDomainFingerprint().toRealmFingerprint() }
             .filter { it.template != null}
             .toRealmList()
         )

}

fun rl_Person.toDomainPerson(): Person =
    Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprints = fingerprints.map(rl_Fingerprint::toDomainFingerprint)
    )

fun Person.toRealmPerson(): rl_Person =
    rl_Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprints = fingerprints.map(Fingerprint::toRealmFingerprint).toRealmList()
    )
