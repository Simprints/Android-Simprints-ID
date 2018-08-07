package com.simprints.id.data.analytics.events.models

import android.arch.persistence.room.Entity
import android.location.Location
import io.realm.RealmObject

open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0): RealmObject() {

    constructor(location: Location): this(location.latitude, location.longitude)
}
