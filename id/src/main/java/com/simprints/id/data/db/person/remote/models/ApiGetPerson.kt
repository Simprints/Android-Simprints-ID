package com.simprints.id.data.db.person.remote.models

import androidx.annotation.Keep
import java.util.*

@Keep
data class ApiGetPerson(val id: String,
                        val projectId: String,
                        val userId: String,
                        val moduleId: String,
                        val createdAt: Date?,
                        val updatedAt: Date?,
                        val fingerprints: List<ApiFingerprintSample>? = null,
                        var faces: List<ApiFaceSample>? = null,
                        val deleted: Boolean)
