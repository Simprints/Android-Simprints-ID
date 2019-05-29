package com.simprints.id.secure.models

import androidx.annotation.Keep

@Keep
data class NonceScope(val projectId: String = "", val userId: String = "")
