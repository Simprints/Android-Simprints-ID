package com.simprints.infra.authstore.domain.models

data class Token(
    val value: String,
    val projectId: String,
    val apiKey: String,
    val applicationId: String,
)
