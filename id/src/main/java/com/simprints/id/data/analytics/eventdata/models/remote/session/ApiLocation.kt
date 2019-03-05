package com.simprints.id.data.analytics.eventdata.models.remote.session

import com.simprints.id.data.analytics.eventdata.models.domain.session.Location

open class ApiLocation(var latitude: Double = 0.0,
                       var longitude: Double = 0.0) {

    constructor(location: Location) :
        this(location.latitude, location.longitude)
}
