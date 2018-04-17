package com.simprints.id.data.db.local.realm.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class rl_Project : RealmObject {

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
