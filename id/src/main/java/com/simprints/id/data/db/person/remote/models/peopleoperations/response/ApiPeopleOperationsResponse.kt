package com.simprints.id.data.db.person.remote.models.peopleoperations.response

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.domain.modality.Modes

@Keep
data class ApiPeopleOperationsResponse(
    val groups: List<ApiPeopleOperationGroupResponse>
)

fun ApiPeopleOperationsResponse.toDomainPeopleCount(projectId: String,
                                                    userId: String?,
                                                    moduleId: List<String?>?,
                                                    modes: List<Modes>?) = groups.mapIndexed { index, response ->
    with (response.counts) {
        PeopleCount(projectId, userId, moduleId?.get(index), modes, create + update + delete)
    }
}
