package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
data class PeopleCount(val projectId: String, val userId: String, val moduleId: String, val modes: List<String>, var count: Int)
