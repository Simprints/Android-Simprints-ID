package com.simprints.infra.config.domain.models

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val creator: String,
    val imageBucket: String,
    val baseUrl: String?,
    val tokenizationKeys: Map<TokenKeyType, TokenizationKeyData>?
)

enum class TokenKeyType {
    AttendantId, ModuleId, Unknown
}

data class TokenizationKeyData(
    val primaryKeyId: Long,
    val typeUrl: String,
    val value: String,
    val keyMaterialType: KeyMaterialType,
    val isEnabled: Boolean,
    val keyId: Long,
    val outputPrefixType: String
)

enum class KeyMaterialType {
    Symmetric, Unknown
}
