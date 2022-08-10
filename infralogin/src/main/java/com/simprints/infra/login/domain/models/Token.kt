package com.simprints.infra.login.domain.models

data class Token(
    val value: String,
    val projectId: String,
    val apiKey: String,
    val applicationId: String
)
