package com.simprints.id.data.db.event.domain.models.session

import androidx.annotation.Keep
import java.util.*

@Keep
data class Location(var latitude: Double = 0.0,
                    var longitude: Double = 0.0,
                    var id: String = UUID.randomUUID().toString())
