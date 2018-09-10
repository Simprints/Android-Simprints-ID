package com.simprints.id.data.analytics.eventData.models.session

import android.location.Location
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    @PrimaryKey var id: String = UUID.randomUUID().toString()) : RealmObject() {

    constructor(location: Location): this(location.latitude, location.longitude)
}
