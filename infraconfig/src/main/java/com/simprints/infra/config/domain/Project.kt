package com.simprints.infra.config.domain

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val creator: String,
    val imageBucket: String,
)
