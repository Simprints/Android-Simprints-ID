package com.simprints.infra.config.local.migrations.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class DbProject : RealmObject() {
    @PrimaryKey
    @Required
    var id: String = ""

    @Required
    var name: String = ""

    @Required
    var description: String = ""

    @Required
    var creator: String = ""

    @Required
    var imageBucket: String = ""

    @Required
    var updatedAt: String = ""
}
