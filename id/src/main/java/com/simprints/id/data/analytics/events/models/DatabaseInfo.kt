package com.simprints.id.data.analytics.events.models

import android.arch.persistence.room.Entity
import io.realm.RealmObject

open class DatabaseInfo(var recordCount: Int = 0) : RealmObject()
