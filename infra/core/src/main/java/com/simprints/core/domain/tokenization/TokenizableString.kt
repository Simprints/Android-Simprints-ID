package com.simprints.core.domain.tokenization

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString.Raw
import com.simprints.core.domain.tokenization.TokenizableString.Tokenized
import com.simprints.core.domain.tokenization.serialization.TokenizableStringDefaultSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Sealed class for values that might be tokenized (symmetrically encrypted). Use this wrapping
 * class when there is a need to differentiate between the encrypted and unencrypted string values.
 *
 * Use [Tokenized] for the encrypted values
 * Use [Raw] for the unencrypted values
 */

@OptIn(ExperimentalSerializationApi::class)
@Parcelize
@Keep
@Serializable(with = TokenizableStringDefaultSerializer::class)
@SerialName("TokenizableString")
@JsonClassDiscriminator("className")
sealed class TokenizableString : Parcelable {
    abstract val value: String
    abstract val className: String

    @Parcelize
    @Serializable
    @SerialName("TokenizableString.Tokenized")
    data class Tokenized(
        override val value: String,
        override val className: String = "TokenizableString.Tokenized",
    ) : TokenizableString() {
        override fun hashCode() = super.hashCode()

        override fun equals(other: Any?) = super.equals(other)

        override fun toString() = super.toString()
    }

    @Serializable
    @SerialName("TokenizableString.Raw")
    data class Raw(
        override val value: String,
        override val className: String = "TokenizableString.Raw",
    ) : TokenizableString() {
        override fun hashCode() = super.hashCode()

        override fun equals(other: Any?) = super.equals(other)

        override fun toString() = super.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return other is TokenizableString && other.value == value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}

fun String.asTokenizableRaw() = Raw(this)

fun String.asTokenizableEncrypted() = Tokenized(this)

fun String.asTokenizable(isTokenized: Boolean) = if (isTokenized) Tokenized(this) else Raw(this)

fun TokenizableString.isTokenized() = when (this) {
    is Tokenized -> true
    is Raw -> false
}

fun List<TokenizableString>.values(): List<String> = map(TokenizableString::value)

fun <T> TokenizableString.takeIfTokenized(value: T): T? = when (this) {
    is Raw -> null
    is Tokenized -> value
}
