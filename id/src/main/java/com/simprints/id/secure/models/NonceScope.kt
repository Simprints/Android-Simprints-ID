package com.simprints.id.secure.models

import com.google.gson.annotations.SerializedName
import com.simprints.id.secure.AuthManager

data class NonceScope(
    @SerializedName(AuthManager.projectIdHeaderKey) val projectId: String = "",
    @SerializedName(AuthManager.userIdHeaderKey) val userId: String = "")
