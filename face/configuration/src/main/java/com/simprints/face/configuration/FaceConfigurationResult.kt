package com.simprints.face.configuration

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceConfigurationResult(
    val isSuccess: Boolean,
    val error: AppErrorReason? = null,
) : Parcelable
