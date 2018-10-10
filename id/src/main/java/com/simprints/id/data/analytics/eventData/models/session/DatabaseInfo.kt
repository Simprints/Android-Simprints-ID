package com.simprints.id.data.analytics.eventData.models.session

import com.simprints.id.tools.json.SkipSerialisationField
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class DatabaseInfo(var recordCount: Int = 0,
                        var sessionCount: Int = 0,
                        @PrimaryKey
                        @SkipSerialisationField
                        var id: String = UUID.randomUUID().toString()) : RealmObject()
