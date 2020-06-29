package com.simprints.id.data.db.event.local.models

import com.simprints.id.data.db.event.domain.events.session.Location
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DbLocation : RealmObject {

    @PrimaryKey
    lateinit var id: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor()
    constructor(location: Location) : this() {
        id = location.id
        latitude = location.latitude
        longitude = location.longitude
    }
}

fun DbLocation.toDomainLocation(): Location = Location(latitude, longitude, id)

