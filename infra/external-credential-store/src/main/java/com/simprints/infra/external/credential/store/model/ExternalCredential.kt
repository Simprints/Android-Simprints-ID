package com.simprints.infra.external.credential.store.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

sealed class ExternalCredential {

    abstract val subjectId: String?

    @Keep
    @Parcelize
    data class QrCode(
        override val subjectId: String?,
        val data: String,
    ) : ExternalCredential(), Parcelable
}
