package com.simprints.id.data.analytics.eventData.models.session

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class DatabaseInfo(var recordCount: Int = 0,
                        @PrimaryKey var id: String = UUID.randomUUID().toString()) : RealmObject()
