package com.simprints.infra.events.event.domain.models

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.TokenKeyType
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.reflect.full.starProjectedType

@Serializable
sealed class Event {
    abstract val id: String

    abstract val type: EventType
    abstract val payload: EventPayload

    abstract var scopeId: String?
    abstract var projectId: String?

    open fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    abstract fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event

    final override fun equals(other: Any?): Boolean = other is Event && other.id == id

    final override fun hashCode(): Int = id.hashCode()

    fun toJson(): String {
        // Get the serializer for the specific runtime class (e.g., ConsentEvent)
        val serializer = serializer(this::class.starProjectedType)

        // Encode using that specific serializer
        // We cast to KSerializer<Any> because generic strictness isn't needed here
        // since we are just serializing to String.
        return JsonHelper.json.encodeToString(serializer, this)
    }
}
