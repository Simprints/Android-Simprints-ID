package com.simprints.id.secure.domain

import com.google.gson.annotations.SerializedName

data class NonceScope(
    @SerializedName("X-ProjectId") val projectId: String,
    @SerializedName("X-UserId") val userId: String)
