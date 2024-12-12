package com.simprints.infra.config.store.models

data class Project(
    val id: String,
    val name: String,
    val state: ProjectState,
    val description: String,
    val creator: String,
    val imageBucket: String,
    val baseUrl: String?,
    val tokenizationKeys: Map<TokenKeyType, String>, // Value is JSON string for DAEAD keyset in TINK
)

enum class TokenKeyType {
    AttendantId,
    ModuleId,
    Unknown,
}
