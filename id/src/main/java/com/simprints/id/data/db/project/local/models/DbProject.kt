package com.simprints.id.data.db.project.local.models

import com.simprints.id.data.db.project.domain.Project
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required


open class DbProject : RealmObject() {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    @PrimaryKey
    @Required var id: String = ""
    @Required var name: String = ""
    @Required var description: String = ""
    @Required var creator: String = ""
    @Required var updatedAt: String = ""
}


fun DbProject.toDomainProject(): Project = Project().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name

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
    it.updatedAt = updatedAt ?: ""
}
