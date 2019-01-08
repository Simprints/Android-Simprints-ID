package com.simprints.id.data.analytics.eventData.models.domain.session

import java.util.*

open class DatabaseInfo(var recordCount: Int = 0,
                        var sessionCount: Int = 0,
                        var id: String = UUID.randomUUID().toString())
