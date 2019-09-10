package com.simprints.id.data.db.peoplecount

import androidx.annotation.Keep
import com.simprints.id.domain.modality.Modes

@Keep
data class PeopleCount(val projectId: String,
                       val userId: String?,
                       val moduleId: String?,
                       val modes: List<Modes>?,
                       var count: Int)
