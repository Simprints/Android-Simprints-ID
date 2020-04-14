package com.simprints.id.data.db.session.domain.models.session

import androidx.annotation.Keep
import java.util.*

@Keep
open class DatabaseInfo(val sessionCount: Int,
                        var recordCount: Int? = null,
                        var id: String = UUID.randomUUID().toString())
