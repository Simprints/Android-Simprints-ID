package com.simprints.id.data.db.session.local.models

import com.simprints.id.data.db.session.local.models.DbModality.Companion.MODALITY_FACE
import com.simprints.id.data.db.session.local.models.DbModality.Companion.MODALITY_FINGER
import com.simprints.id.domain.modality.Modality
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbModality() : RealmObject() {

    @PrimaryKey lateinit var name: String

    constructor(modality: Modality) : this() {
        name = modality.name
    }

    companion object {
        const val MODALITY_FACE = "FACE"
        const val MODALITY_FINGER = "FINGER"
    }

}

fun DbModality.toDomain(): Modality = when (name) {
    MODALITY_FACE -> Modality.FACE
    MODALITY_FINGER -> Modality.FINGER
    else -> throw IllegalStateException("Must be either FACE or FINGER")
}
