package com.simprints.id.sync.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RealmSyncInfo(@field:PrimaryKey var id: Int = 0,
                         var lastSyncTime: Date = Date(0)) : RealmObject()
