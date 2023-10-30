package com.simprints.fingerprint.capture.models

import androidx.annotation.Keep
import com.simprints.moduleapi.common.ISecuredImageRef
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SecuredImageRef(override val path: Path) : ISecuredImageRef
