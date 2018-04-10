package com.simprints.id.data.db.local.realm

import com.simprints.id.domain.Constants
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RealmSyncInfo(@field:PrimaryKey var syncGroupId: Int = 0,
                         var lastSyncTime: Date = Date(0)) : RealmObject() {

    constructor(syncGroup: Constants.GROUP) : this(
        syncGroupId = syncGroup.ordinal
    )

}
