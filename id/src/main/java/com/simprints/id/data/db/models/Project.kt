package com.simprints.id.data.db.models

import com.google.gson.annotations.Expose
import io.realm.RealmObject

open class Project : RealmObject {

    @Expose
    lateinit var projectId: String
    @Expose
    lateinit var description: String

    constructor()
}
