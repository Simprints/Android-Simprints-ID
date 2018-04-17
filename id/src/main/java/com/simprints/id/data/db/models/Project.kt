package com.simprints.id.data.db.models

import com.google.gson.annotations.Expose
import io.realm.RealmObject

// Using @Expose because we are using this model to parse the data from the server.
// Because it's a RealmObject too, it inherits a lot of other properties and we want
// Gson to ignore them.
open class Project : RealmObject() {

    companion object {
        const val PROJECT_ID_FIELD = "id"
    }

    @Expose
    lateinit var id: String

    @Expose
    var legacyId: String? = null

    @Expose
    lateinit var name: String

    @Expose
    lateinit var description: String

    @Expose
    lateinit var creator: String

    @Expose
    var updatedAt: String? = null
}
