package com.simprints.id.data.db.person.remote.models.peopleoperations.request

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationWhereLabel(
    val key: String,
    val value: String
)


enum class WhereLabelKey(val key: String) {
    USER("userId"),
    MODULE("moduleId"),
    MODE("mode")
}
