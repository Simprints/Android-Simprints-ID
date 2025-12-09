package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString

/**
 * V1 external schema for tokenizable strings.
 *
 * Sealed class for values that might be tokenized (symmetrically encrypted).
 * Use this wrapping class when there is a need to differentiate between
 * encrypted and unencrypted string values.
 *
 * This is a stable external contract decoupled from internal domain models.
 */
@Keep
sealed class TokenizableStringV1 {
    abstract val value: String

    /**
     * Represents a tokenized (encrypted) string value.
     */
    data class Tokenized(
        override val value: String,
    ) : TokenizableStringV1() {
        override fun hashCode() = super.hashCode()

        override fun equals(other: Any?) = super.equals(other)

        override fun toString() = super.toString()
    }

    /**
     * Represents a raw (unencrypted) string value.
     */
    data class Raw(
        override val value: String,
    ) : TokenizableStringV1() {
        override fun hashCode() = super.hashCode()

        override fun equals(other: Any?) = super.equals(other)

        override fun toString() = super.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return other is TokenizableStringV1 && other.value == value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}

/**
 * Converts internal TokenizableString to V1 external schema.
 */
fun TokenizableString.toCoSyncV1(): TokenizableStringV1 = when (this) {
    is TokenizableString.Tokenized -> TokenizableStringV1.Tokenized(value)
    is TokenizableString.Raw -> TokenizableStringV1.Raw(value)
}

/**
 * Converts V1 external schema to internal TokenizableString.
 */
fun TokenizableStringV1.toDomain(): TokenizableString = when (this) {
    is TokenizableStringV1.Tokenized -> TokenizableString.Tokenized(value)
    is TokenizableStringV1.Raw -> TokenizableString.Raw(value)
}
