package com.simprints.infra.config.local.models

import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.extentions.filterNotNullValues
import com.simprints.core.tools.extentions.singleItemList
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.domain.models.KeyMaterialType
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.domain.models.TokenizationKeyData
import com.simprints.infra.logging.Simber

private const val KEY_ENABLED = "enabled"
internal fun Project.toProto(): ProtoProject =
    ProtoProject.newBuilder()
        .setId(id)
        .setCreator(creator)
        .setDescription(description)
        .setName(name)
        .setImageBucket(imageBucket)
        .also {
            if (baseUrl != null) it.baseUrl = baseUrl
            tokenizationKeys?.mapKeys { entry ->
                entry.key.toString()
            }?.mapValues { entry ->
                JsonHelper.toJson(TokenizationItem.fromTokenizationKeyData(entry.value))
            }?.let { map ->
                it.putAllTokenizationKeys(map)
            }
        }
        .build()

internal fun ProtoProject.toDomain(): Project {
    val tokenizationKeys = tokenizationKeysMap.asTokenizationKeysMap()
    return Project(
        id = id,
        name = name,
        description = description,
        creator = creator,
        imageBucket = imageBucket,
        baseUrl = baseUrl,
        tokenizationKeys = tokenizationKeys
    )
}

/**
 * Maps remote response map of type of String <-> JsonString to the map of [TokenKeyType] <-> [TokenizationKeyData]
 */
internal fun Map<String, String>?.asTokenizationKeysMap(): Map<TokenKeyType, TokenizationKeyData> {
    return (this ?: emptyMap()).mapKeys { entry ->
        runCatching { TokenKeyType.valueOf(entry.key) }.getOrElse { TokenKeyType.Unknown }
    }.mapValues { entry ->
        try {
            JsonHelper.fromJson(
                json = entry.value,
                type = object : TypeReference<TokenizationItem>() {}
            ).toTokenizationKeyData()
        } catch (e: Exception) {
            Simber.e(e)
            return@mapValues null
        }
    }.filterNotNullValues()
}

/**
 * Data class that contains the following JSON structure which is received from the backend:
 *
 * {
 *   "primaryKeyId": 987654432,
 *   "key": [
 *     {
 *       "keyData": {
 *         "typeUrl": "typeUrl",
 *         "value": "AzT...b12C",
 *         "keyMaterialType": "SYMMETRIC"
 *       },
 *       "status": "ENABLED",
 *       "keyId": 945828840,
 *       "outputPrefixType": "TINK"
 *     }
 *   ]
 * }
 */
private data class TokenizationItem(
    val primaryKeyId: Long,
    val key: List<TokenizationKey>,
) {
    companion object {
        fun fromTokenizationKeyData(data: TokenizationKeyData): TokenizationItem {
            val status = if (data.isEnabled) KEY_ENABLED else "disabled"
            return TokenizationItem(
                primaryKeyId = data.primaryKeyId,
                key = TokenizationKey(
                    keyData = KeyData(
                        typeUrl = data.typeUrl,
                        value = data.value,
                        keyMaterialType = data.keyMaterialType.toString()
                    ),
                    status = status,
                    keyId = data.keyId,
                    outputPrefixType = data.outputPrefixType
                ).singleItemList()
            )
        }
    }

    fun toTokenizationKeyData(): TokenizationKeyData? {
        val tokenizationKey = key.firstOrNull() ?: return null
        return TokenizationKeyData(
            primaryKeyId = primaryKeyId,
            typeUrl = tokenizationKey.keyData.typeUrl,
            value = tokenizationKey.keyData.value,
            keyMaterialType = runCatching { KeyMaterialType.valueOf(tokenizationKey.keyData.keyMaterialType) }.getOrElse { KeyMaterialType.Unknown },
            isEnabled = tokenizationKey.status.equals(KEY_ENABLED, ignoreCase = true),
            keyId = tokenizationKey.keyId,
            outputPrefixType = tokenizationKey.outputPrefixType
        )
    }
}

private data class TokenizationKey(
    val keyData: KeyData,
    val status: String,
    val keyId: Long,
    val outputPrefixType: String,
)

private data class KeyData(
    val typeUrl: String,
    val value: String,
    val keyMaterialType: String,
)
