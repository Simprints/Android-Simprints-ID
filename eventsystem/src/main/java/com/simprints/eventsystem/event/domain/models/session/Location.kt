package com.simprints.eventsystem.event.domain.models.session

import androidx.annotation.Keep

@Keep
data class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0)
