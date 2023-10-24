package com.simprints.face.configuration

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.moduleapi.app.responses.IAppErrorReason
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FaceConfigurationResult(
    val isSuccess: Boolean,
    val error: IAppErrorReason? = null,
) : Parcelable
