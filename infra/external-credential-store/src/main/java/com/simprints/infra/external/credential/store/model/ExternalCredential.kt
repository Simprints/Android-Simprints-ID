package com.simprints.infra.external.credential.store.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Keep
@Parcelize
data class ExternalCredential(
    val data: String,
    val subjectId: String?,
) : Parcelable, Serializable
