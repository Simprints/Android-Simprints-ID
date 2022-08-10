package com.simprints.infra.config.domain.models

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val creator: String,
    val imageBucket: String,
)
