package com.simprints.id.data.db.project.domain

import androidx.annotation.Keep

@Keep
data class Project(val id: String,
                   val name: String,
                   val description: String,
                   val creator: String,
                   val updatedAt: String? = null)
