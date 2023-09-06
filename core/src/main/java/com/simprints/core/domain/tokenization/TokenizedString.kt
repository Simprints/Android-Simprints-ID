package com.simprints.core.domain.tokenization

import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.simprints.core.domain.tokenization.TokenizedString.Encrypted
import com.simprints.core.domain.tokenization.TokenizedString.Raw
import kotlinx.parcelize.Parcelize

/**
 * Sealed class for values that might be tokenized (symmetrically encrypted). Use this wrapping
 * class when there is a need to differentiate between the encrypted and unencrypted string values.
 *
 * Use [Encrypted] for the encrypted values
 * Use [Raw] for the unencrypted values
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "className"
)
@Keep
sealed class TokenizedString : Parcelable {
    abstract val value: String

    @JsonTypeName("Encrypted")
    @Parcelize
    data class Encrypted(override val value: String) : TokenizedString()

    @JsonTypeName("Raw")
    @Parcelize
    data class Raw(override val value: String) : TokenizedString()
}

fun TokenizedString?.orEmpty() = TokenizedString.Raw("")
fun String.asTokenizedRaw() = TokenizedString.Raw(this)
fun String.asTokenizedEncrypted() = TokenizedString.Encrypted(this)