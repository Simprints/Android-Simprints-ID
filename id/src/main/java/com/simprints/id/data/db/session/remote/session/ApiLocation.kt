package com.simprints.id.data.db.session.remote.session

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.session.Location

@Keep
open class ApiLocation(var latitude: Double = 0.0,
                       var longitude: Double = 0.0) {

    constructor(location: Location) :
        this(location.latitude, location.longitude)
}
