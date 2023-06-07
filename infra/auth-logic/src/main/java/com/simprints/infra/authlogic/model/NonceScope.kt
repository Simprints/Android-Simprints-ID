package com.simprints.infra.authlogic.model

import androidx.annotation.Keep

@Keep
data class NonceScope(
    val projectId: String = "",
    val userId: String = "",
)
