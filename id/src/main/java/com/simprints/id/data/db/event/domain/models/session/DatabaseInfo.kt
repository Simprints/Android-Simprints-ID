package com.simprints.id.data.db.event.domain.models.session

import androidx.annotation.Keep
import java.util.*

@Keep
data class DatabaseInfo(val sessionCount: Int,
                        var recordCount: Int? = null,
                        var id: String = UUID.randomUUID().toString())
