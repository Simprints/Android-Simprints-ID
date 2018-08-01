package com.simprints.id.data.analytics

import io.realm.RealmObject

open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0) : RealmObject()
