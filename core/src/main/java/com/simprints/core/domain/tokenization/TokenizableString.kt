package com.simprints.core.domain.tokenization

import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.simprints.core.domain.tokenization.TokenizableString.Raw
import com.simprints.core.domain.tokenization.TokenizableString.Tokenized
import kotlinx.parcelize.Parcelize

/**
 * Sealed class for values that might be tokenized (symmetrically encrypted). Use this wrapping
 * class when there is a need to differentiate between the encrypted and unencrypted string values.
 *
 * Use [Tokenized] for the encrypted values
 * Use [Raw] for the unencrypted values
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "className"
)
@Keep
sealed class TokenizableString : Parcelable {
    abstract val value: String

    @JsonTypeName("Encrypted")
    @Parcelize
    data class Tokenized(override val value: String) : TokenizableString() {
        override fun hashCode() = super.hashCode()
        override fun equals(other: Any?) = super.equals(other)
        override fun toString() = super.toString()
    }

    @JsonTypeName("Raw")
    @Parcelize
    data class Raw(override val value: String) : TokenizableString() {
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

fun String.asTokenized(isTokenized: Boolean) =
    if (isTokenized) this.asTokenizedEncrypted() else this.asTokenizedRaw()

fun String.asTokenizedRaw() = Raw(this)
fun String.asTokenizedEncrypted() = Tokenized(this)

fun TokenizableString.isTokenized() = when (this) {
    is Tokenized -> true
    is Raw -> false
}

fun List<TokenizableString>.values(): List<String> = map(TokenizableString::value)

fun <T> TokenizableString.takeIfTokenized(value: T): T? = when (this) {
    is Raw -> null
    is Tokenized -> value
}