package com.simprints.id.data.analytics.eventdata.models.domain.session

import androidx.annotation.Keep
import java.util.*

@Keep
open class DatabaseInfo(var recordCount: Int = 0,
                        var sessionCount: Int = 0,
                        var id: String = UUID.randomUUID().toString())
