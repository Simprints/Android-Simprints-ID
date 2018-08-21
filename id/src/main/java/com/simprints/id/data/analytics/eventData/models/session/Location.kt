package com.simprints.id.data.analytics.eventData.models.session

import android.location.Location
import io.realm.RealmObject

open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0): RealmObject() {

    constructor(location: Location): this(location.latitude, location.longitude)
}
