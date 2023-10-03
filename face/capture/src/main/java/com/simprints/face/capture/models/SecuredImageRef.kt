package com.simprints.face.capture.models

import androidx.annotation.Keep
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
internal data class SecuredImageRef(override val path: Path) : ISecuredImageRef

