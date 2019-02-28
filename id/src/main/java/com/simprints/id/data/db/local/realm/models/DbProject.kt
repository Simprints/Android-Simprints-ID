package com.simprints.id.data.db.local.realm.models

import com.simprints.id.domain.Project
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class DbProject : RealmObject {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    @PrimaryKey
    var id: String = ""
    var legacyId: String = ""

    var name: String = ""
    var description: String = ""
    var creator: String = ""
    var updatedAt: String = ""

    constructor()

}


fun DbProject.toDomainProject(): Project = Project().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name

    it.legacyId =
        if (legacyId.isBlank())
            null
        else legacyId

    it.updatedAt =
        if (updatedAt.isBlank())
            null
        else updatedAt
}

fun Project.toRealmProject(): DbProject = DbProject().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name
    it.legacyId = legacyId ?: ""
    it.updatedAt = updatedAt ?: ""
}
