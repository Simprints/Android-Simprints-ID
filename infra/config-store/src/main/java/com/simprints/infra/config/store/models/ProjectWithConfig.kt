package com.simprints.infra.config.store.models

data class ProjectWithConfig(
    val project: Project,
    val configuration: ProjectConfiguration,
)
