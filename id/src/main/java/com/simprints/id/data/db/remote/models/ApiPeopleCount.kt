package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.simprints.id.domain.PeopleCount

@Keep
data class ApiPeopleCount(val projectId: String, val userId: String, val moduleId: String, val modes: List<String>, val count: Int)

fun ApiPeopleCount.toDomainPeopleCount() = PeopleCount(projectId, userId, moduleId, modes, count)
