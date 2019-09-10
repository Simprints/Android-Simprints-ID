package com.simprints.id.data.db.person.remote.models

import androidx.annotation.Keep
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.modality.Modes

@Keep
data class ApiPeopleCount(val projectId: String, val userId: String, val moduleId: String, val modes: List<ApiModes>, val count: Int)

fun ApiPeopleCount.toDomainPeopleCount() = PeopleCount(projectId, userId, moduleId,
    modes.map { Modes.valueOf(it.name) }, count)
