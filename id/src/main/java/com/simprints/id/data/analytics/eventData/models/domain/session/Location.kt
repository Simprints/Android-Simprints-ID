package com.simprints.id.data.analytics.eventData.models.domain.session

import java.util.*

open class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    var id: String = UUID.randomUUID().toString())
